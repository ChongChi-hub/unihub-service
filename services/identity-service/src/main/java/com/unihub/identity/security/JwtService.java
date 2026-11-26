package com.unihub.identity.security;

import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Signs and verifies RS256 JWT access tokens. */
@Service
public class JwtService {

	private static final Logger log = LoggerFactory.getLogger(JwtService.class);

	private final JwtProperties properties;
	private final PrivateKey privateKey;
	private final PublicKey publicKey;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		if (StringUtils.hasText(properties.privateKeyPem()) && StringUtils.hasText(properties.publicKeyPem())) {
			this.privateKey = parsePrivateKey(properties.privateKeyPem());
			this.publicKey = parsePublicKey(properties.publicKeyPem());
		} else {
			log.warn("No JWT RSA keys configured; generating ephemeral dev keypair");
			KeyPair pair = generateRsaKeyPair();
			this.privateKey = pair.getPrivate();
			this.publicKey = pair.getPublic();
		}
	}

	public String generateAccessToken(UserEntity user) {
		Instant now = Instant.now();
		List<String> roles = user.getRoles().stream().map(RoleEntity::getName).sorted().toList();
		return Jwts.builder().id(UUID.randomUUID().toString()).issuer(properties.issuer())
				.subject(user.getId().toString()).claim("email", user.getEmail()).claim("fullName", user.getFullName())
				.claim("roles", roles).issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(properties.accessExpirationMs())))
				.signWith(privateKey, Jwts.SIG.RS256).compact();
	}

	public Claims parseAndVerify(String token) {
		return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
	}

	public String getPublicKeyBase64() {
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}

	private static KeyPair generateRsaKeyPair() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			return generator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to generate RSA keypair", ex);
		}
	}

	private static PrivateKey parsePrivateKey(String pem) {
		try {
			String normalized = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
					.replaceAll("\\s", "");
			byte[] decoded = Base64.getDecoder().decode(normalized);
			return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
		} catch (Exception ex) {
			throw new IllegalStateException("Invalid RSA private key PEM", ex);
		}
	}

	private static PublicKey parsePublicKey(String pem) {
		try {
			String normalized = pem.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
					.replaceAll("\\s", "");
			byte[] decoded = Base64.getDecoder().decode(normalized);
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
		} catch (Exception ex) {
			throw new IllegalStateException("Invalid RSA public key PEM", ex);
		}
	}
}
