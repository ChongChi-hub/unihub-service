package com.unihub.identity.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String issuer, long accessExpirationMs, long refreshExpirationMs, String privateKeyPem,
		String publicKeyPem) {
}
