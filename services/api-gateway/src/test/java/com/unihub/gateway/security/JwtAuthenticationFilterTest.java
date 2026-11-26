package com.unihub.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationFilterTest {

	@Test
	void allowsPublicLoginPathWithoutToken() {
		PublicKeyProvider provider = new PublicKeyProvider("", "http://localhost:8081/api/v1/auth/public-key");
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

		MockServerWebExchange exchange = MockServerWebExchange
				.from(MockServerHttpRequest.post("/api/v1/auth/login").build());
		GatewayFilterChain chain = ex -> {
			assertThat(ex.getRequest().getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
			return Mono.empty();
		};

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
	}

	@Test
	void rejectsProtectedPathWithoutBearerToken() {
		PublicKeyProvider provider = new PublicKeyProvider("", "http://localhost:8081/api/v1/auth/public-key");
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/users").build());

		StepVerifier.create(filter.filter(exchange, ex -> Mono.empty())).verifyComplete();
		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void forwardsUserHeadersWhenJwtIsValid() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		KeyPair pair = gen.generateKeyPair();

		String publicBase64 = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
		PublicKeyProvider provider = new PublicKeyProvider(publicBase64, "");
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

		String token = Jwts.builder().subject("user-123").claim("email", "admin@unihub.edu.vn")
				.claim("roles", List.of("ADMIN")).issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plusSeconds(300))).signWith(pair.getPrivate(), Jwts.SIG.RS256)
				.compact();

		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/users")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build());

		GatewayFilterChain chain = ex -> {
			assertThat(ex.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("user-123");
			assertThat(ex.getRequest().getHeaders().getFirst("X-User-Email")).isEqualTo("admin@unihub.edu.vn");
			assertThat(ex.getRequest().getHeaders().getFirst("X-User-Roles")).isEqualTo("ADMIN");
			return Mono.empty();
		};

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
	}
}
