package com.unihub.identity.security;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Redis-backed brute-force login rate limiter. */
@Component
public class LoginRateLimiter {

	private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);
	private static final String KEY_PREFIX = "auth:login:fail:";

	private final StringRedisTemplate redisTemplate;
	private final int maxAttempts;
	private final Duration lockDuration;

	public LoginRateLimiter(StringRedisTemplate redisTemplate,
			@Value("${app.auth.max-failed-attempts:5}") int maxAttempts,
			@Value("${app.auth.lock-duration-seconds:300}") long lockDurationSeconds) {
		this.redisTemplate = redisTemplate;
		this.maxAttempts = maxAttempts;
		this.lockDuration = Duration.ofSeconds(lockDurationSeconds);
	}

	public boolean isBlocked(String email) {
		try {
			String value = redisTemplate.opsForValue().get(KEY_PREFIX + email.toLowerCase());
			return value != null && Integer.parseInt(value) >= maxAttempts;
		} catch (Exception ex) {
			log.warn("Redis unavailable for login rate limit check: {}", ex.getMessage());
			return false;
		}
	}

	public void recordFailure(String email) {
		try {
			String key = KEY_PREFIX + email.toLowerCase();
			Long count = redisTemplate.opsForValue().increment(key);
			if (count != null && count == 1L) {
				redisTemplate.expire(key, lockDuration);
			}
		} catch (Exception ex) {
			log.warn("Redis unavailable for login rate limit record: {}", ex.getMessage());
		}
	}

	public void reset(String email) {
		try {
			redisTemplate.delete(KEY_PREFIX + email.toLowerCase());
		} catch (Exception ex) {
			log.warn("Redis unavailable for login rate limit reset: {}", ex.getMessage());
		}
	}
}
