package com.unihub.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global gateway filter enforcing JWT auth on protected routes and propagating
 * user headers.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private static final String CORRELATION_HEADER = "X-Correlation-Id";
	private static final List<String> PUBLIC_PREFIXES = List.of("/api/v1/auth/login", "/api/v1/auth/refresh",
			"/api/v1/auth/logout", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
			"/api/v1/auth/public-key", "/actuator/", "/v3/api-docs", "/identity/v3/api-docs", "/academic/v3/api-docs",
			"/swagger-ui");

	private final PublicKeyProvider publicKeyProvider;

	public JwtAuthenticationFilter(PublicKeyProvider publicKeyProvider) {
		this.publicKeyProvider = publicKeyProvider;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);
		if (!StringUtils.hasText(correlationId)) {
			correlationId = UUID.randomUUID().toString();
		}
		final String finalCorrelationId = correlationId;

		String path = exchange.getRequest().getURI().getPath();
		if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS || isPublicPath(path)) {
			ServerHttpRequest mutated = exchange.getRequest().mutate().header(CORRELATION_HEADER, finalCorrelationId)
					.build();
			return chain.filter(exchange.mutate().request(mutated).build());
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		String token = authHeader.substring(7);
		return publicKeyProvider.getPublicKey().flatMap(publicKey -> {
			Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();

			@SuppressWarnings("unchecked")
			List<String> roles = claims.get("roles", List.class);
			String rolesHeader = roles == null ? "" : String.join(",", roles);

			ServerHttpRequest mutated = exchange.getRequest().mutate().header(CORRELATION_HEADER, finalCorrelationId)
					.header("X-User-Id", claims.getSubject()).header("X-User-Email", claims.get("email", String.class))
					.header("X-User-Roles", rolesHeader).build();

			return chain.filter(exchange.mutate().request(mutated).build());
		}).onErrorResume(ex -> {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		});
	}

	@Override
	public int getOrder() {
		return -100;
	}

	private static boolean isPublicPath(String path) {
		return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
	}
}
