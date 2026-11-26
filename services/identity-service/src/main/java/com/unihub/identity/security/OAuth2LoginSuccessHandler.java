package com.unihub.identity.security;

import com.unihub.common.exception.AppException;
import com.unihub.identity.dto.AuthResponse;
import com.unihub.identity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Links an authenticated Google OAuth2 user to an existing Unihub account by
 * email.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthService authService;
	private final String webOrigin;

	public OAuth2LoginSuccessHandler(AuthService authService,
			@Value("${app.web.origin:http://localhost:3000}") String webOrigin) {
		this.authService = authService;
		this.webOrigin = webOrigin;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String email = oAuth2User.getAttribute("email");
		try {
			AuthResponse tokens = authService.loginWithGoogleEmail(email);
			String redirectUrl = UriComponentsBuilder.fromUriString(webOrigin + "/oauth/callback")
					.queryParam("accessToken", tokens.accessToken()).queryParam("refreshToken", tokens.refreshToken())
					.build().toUriString();
			response.sendRedirect(redirectUrl);
		} catch (AppException ex) {
			String encoded = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
			response.sendRedirect(webOrigin + "/login?oauthError=" + encoded);
		}
	}
}
