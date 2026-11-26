package com.unihub.identity.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Redirects failed Google OAuth2 logins back to the web login page with an
 * error parameter.
 */
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

	private final String webOrigin;

	public OAuth2LoginFailureHandler(@Value("${app.web.origin:http://localhost:3000}") String webOrigin) {
		this.webOrigin = webOrigin;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		String encoded = URLEncoder.encode("Đăng nhập Google thất bại. Vui lòng thử lại.", StandardCharsets.UTF_8);
		response.sendRedirect(webOrigin + "/login?oauthError=" + encoded);
	}
}
