package com.unihub.ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.common.api.ErrorResponse;
import com.unihub.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;
	private final ObjectMapper objectMapper;

	public SecurityConfig(GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter,
			ObjectMapper objectMapper) {
		this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
		this.objectMapper = objectMapper;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, authEx) -> writeError(res, req, ErrorCode.UNAUTHORIZED))
						.accessDeniedHandler((req, res, deniedEx) -> writeError(res, req, ErrorCode.FORBIDDEN)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
						.permitAll().anyRequest().authenticated())
				.addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	private void writeError(HttpServletResponse response, HttpServletRequest request, ErrorCode code)
			throws IOException {
		response.setStatus(code.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(),
				ErrorResponse.of(code.getCode(), code.getDefaultMessage(), request.getRequestURI()));
	}
}
