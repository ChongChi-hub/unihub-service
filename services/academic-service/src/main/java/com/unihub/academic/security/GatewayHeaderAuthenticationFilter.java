package com.unihub.academic.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** Populates Spring SecurityContext from gateway-forwarded X-User-* headers. */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String userIdHeader = request.getHeader("X-User-Id");
		String emailHeader = request.getHeader("X-User-Email");
		String rolesHeader = request.getHeader("X-User-Roles");

		if (SecurityContextHolder.getContext().getAuthentication() == null && StringUtils.hasText(userIdHeader)) {
			List<String> roles = StringUtils.hasText(rolesHeader)
					? Arrays.stream(rolesHeader.split(",")).map(String::trim).filter(StringUtils::hasText).toList()
					: List.of();
			List<SimpleGrantedAuthority> authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r))
					.toList();
			GatewayUserPrincipal principal = new GatewayUserPrincipal(UUID.fromString(userIdHeader), emailHeader,
					roles);
			SecurityContextHolder.getContext()
					.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));
		}
		filterChain.doFilter(request, response);
	}

	public record GatewayUserPrincipal(UUID userId, String email, List<String> roles) {
	}
}
