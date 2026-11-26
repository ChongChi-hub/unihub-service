package com.unihub.gateway.config;

import java.net.InetSocketAddress;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Resolves client IP address for Spring Cloud Gateway Redis RequestRateLimiter.
 */
@Configuration
public class RateLimitConfig {

	@Bean
	public KeyResolver ipKeyResolver() {
		return exchange -> {
			String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
			if (forwardedFor != null && !forwardedFor.isBlank()) {
				return Mono.just(forwardedFor.split(",")[0].trim());
			}
			InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
			String host = remote != null && remote.getAddress() != null
					? remote.getAddress().getHostAddress()
					: "unknown";
			return Mono.just(host);
		};
	}
}
