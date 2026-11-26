package com.unihub.identity.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates Spring SecurityContext from gateway-forwarded headers (X-User-*) or
 * a direct Bearer JWT when called without the gateway.
 */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public GatewayHeaderAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			authenticateFromHeadersOrJwt(request);
		}
		filterChain.doFilter(request, response);
	}

	private void authenticateFromHeadersOrJwt(HttpServletRequest request) {
		String userIdHeader = request.getHeader("X-User-Id");
		String emailHeader = request.getHeader("X-User-Email");
		String rolesHeader = request.getHeader("X-User-Roles");

		if (StringUtils.hasText(userIdHeader)) {
			List<String> roles = StringUtils.hasText(rolesHeader)
					? Arrays.stream(rolesHeader.split(",")).map(String::trim).filter(StringUtils::hasText).toList()
					: List.of();
			setAuthentication(UUID.fromString(userIdHeader), emailHeader, roles);
			return;
		}

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
			try {
				Claims claims = jwtService.parseAndVerify(authHeader.substring(7));
				@SuppressWarnings("unchecked")
				List<String> roles = claims.get("roles", List.class);
				setAuthentication(UUID.fromString(claims.getSubject()), claims.get("email", String.class),
						roles == null ? List.of() : roles);
			} catch (Exception ignored) {
				SecurityContextHolder.clearContext();
			}
		}
	}

	private static void setAuthentication(UUID userId, String email, List<String> roles) {
		List<SimpleGrantedAuthority> authorities = roles.stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
		GatewayUserPrincipal principal = new GatewayUserPrincipal(userId, email, roles);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
				authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
