package com.unihub.identity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.common.api.ErrorResponse;
import com.unihub.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;
	private final ObjectMapper objectMapper;
	private final ObjectProvider<OAuth2LoginSuccessHandler> oAuth2SuccessHandler;
	private final ObjectProvider<OAuth2LoginFailureHandler> oAuth2FailureHandler;
	private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

	public SecurityConfig(GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter,
			ObjectMapper objectMapper, ObjectProvider<OAuth2LoginSuccessHandler> oAuth2SuccessHandler,
			ObjectProvider<OAuth2LoginFailureHandler> oAuth2FailureHandler,
			ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) {
		this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
		this.objectMapper = objectMapper;
		this.oAuth2SuccessHandler = oAuth2SuccessHandler;
		this.oAuth2FailureHandler = oAuth2FailureHandler;
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, authEx) -> writeError(res, req, ErrorCode.UNAUTHORIZED))
						.accessDeniedHandler((req, res, deniedEx) -> writeError(res, req, ErrorCode.FORBIDDEN)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout",
								"/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
								"/api/v1/auth/public-key", "/oauth2/**", "/login/oauth2/**", "/actuator/health",
								"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
						.permitAll().anyRequest().authenticated())
				.addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		if (clientRegistrationRepository.getIfAvailable() != null && oAuth2SuccessHandler.getIfAvailable() != null
				&& oAuth2FailureHandler.getIfAvailable() != null) {
			http.oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler.getObject())
					.failureHandler(oAuth2FailureHandler.getObject()));
		}

		return http.build();
	}

	private void writeError(HttpServletResponse response, HttpServletRequest request, ErrorCode code)
			throws IOException {
		response.setStatus(code.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ErrorResponse body = ErrorResponse.of(code.getCode(), code.getDefaultMessage(), request.getRequestURI());
		objectMapper.writeValue(response.getWriter(), body);
	}
}
