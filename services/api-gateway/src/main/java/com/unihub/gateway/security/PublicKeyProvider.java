package com.unihub.gateway.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** Provides and caches the RSA public key used to verify JWT access tokens. */
@Component
public class PublicKeyProvider {

	private final AtomicReference<PublicKey> cachedKey = new AtomicReference<>();
	private final WebClient webClient;
	private final String publicKeyUrl;

	public PublicKeyProvider(@Value("${app.jwt.public-key-pem:}") String publicKeyPem,
			@Value("${app.jwt.identity-public-key-url:http://localhost:8081/api/v1/auth/public-key}") String publicKeyUrl) {
		this.webClient = WebClient.builder().build();
		this.publicKeyUrl = publicKeyUrl;
		if (StringUtils.hasText(publicKeyPem)) {
			this.cachedKey.set(parsePublicKey(publicKeyPem));
		}
	}

	public Mono<PublicKey> getPublicKey() {
		PublicKey existing = cachedKey.get();
		if (existing != null) {
			return Mono.just(existing);
		}
		return webClient.get().uri(publicKeyUrl).retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				}).map(body -> {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) body.get("data");
					String base64Key = (String) data.get("publicKey");
					PublicKey key = parsePublicKey(base64Key);
					cachedKey.set(key);
					return key;
				});
	}

	static PublicKey parsePublicKey(String pemOrBase64) {
		try {
			String normalized = pemOrBase64.replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
			byte[] decoded = Base64.getDecoder().decode(normalized);
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
		} catch (Exception ex) {
			throw new IllegalStateException("Invalid RSA public key", ex);
		}
	}
}
