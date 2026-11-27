package com.unihub.report.security;

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
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String userId = request.getHeader("X-User-Id");
		String email = request.getHeader("X-User-Email");
		String rolesHeader = request.getHeader("X-User-Roles");
		if (userId != null && !userId.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
			List<String> roles = rolesHeader == null || rolesHeader.isBlank()
					? List.of()
					: Arrays.stream(rolesHeader.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
			List<SimpleGrantedAuthority> authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r))
					.toList();
			GatewayUserPrincipal principal = new GatewayUserPrincipal(UUID.fromString(userId), email, roles);
			SecurityContextHolder.getContext()
					.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));
		}
		filterChain.doFilter(request, response);
	}

	public record GatewayUserPrincipal(UUID userId, String email, List<String> roles) {
	}
}
