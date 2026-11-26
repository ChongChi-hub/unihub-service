package com.unihub.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.identity.domain.PasswordResetTokenEntity;
import com.unihub.identity.domain.RefreshTokenEntity;
import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.domain.UserStatus;
import com.unihub.identity.dto.AuthResponse;
import com.unihub.identity.dto.ChangePasswordRequest;
import com.unihub.identity.dto.ForgotPasswordRequest;
import com.unihub.identity.dto.LoginRequest;
import com.unihub.identity.dto.LogoutRequest;
import com.unihub.identity.dto.RefreshTokenRequest;
import com.unihub.identity.dto.ResetPasswordRequest;
import com.unihub.identity.repository.PasswordResetTokenRepository;
import com.unihub.identity.repository.RefreshTokenRepository;
import com.unihub.identity.repository.UserRepository;
import com.unihub.identity.security.JwtProperties;
import com.unihub.identity.security.JwtService;
import com.unihub.identity.security.LoginRateLimiter;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

	private UserRepository userRepository;
	private RefreshTokenRepository refreshTokenRepository;
	private PasswordResetTokenRepository passwordResetTokenRepository;
	private LoginRateLimiter rateLimiter;
	private EmailService emailService;
	private AuthService authService;
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		refreshTokenRepository = mock(RefreshTokenRepository.class);
		passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
		rateLimiter = mock(LoginRateLimiter.class);
		emailService = mock(EmailService.class);
		passwordEncoder = new BCryptPasswordEncoder();
		JwtProperties properties = new JwtProperties("unihub-identity", 900_000L, 604_800_000L, "", "");
		JwtService jwtService = new JwtService(properties);
		authService = new AuthService(userRepository, refreshTokenRepository, passwordResetTokenRepository,
				passwordEncoder, jwtService, properties, rateLimiter, emailService);
	}

	@Test
	void loginSucceedsForActiveUserWithValidPassword() {
		UserEntity user = sampleUser("sv@unihub.edu.vn", "Secret@123", "STUDENT");
		when(rateLimiter.isBlocked("sv@unihub.edu.vn")).thenReturn(false);
		when(userRepository.findByEmailIgnoreCase("sv@unihub.edu.vn")).thenReturn(Optional.of(user));

		AuthResponse response = authService.login(new LoginRequest("sv@unihub.edu.vn", "Secret@123"));

		assertThat(response.accessToken()).isNotBlank();
		assertThat(response.refreshToken()).isNotBlank();
		assertThat(response.user().roles()).containsExactly("STUDENT");
		verify(rateLimiter).reset("sv@unihub.edu.vn");
		verify(refreshTokenRepository).save(any());
	}

	@Test
	void loginRejectsWhenRateLimited() {
		when(rateLimiter.isBlocked("sv@unihub.edu.vn")).thenReturn(true);

		assertThatThrownBy(() -> authService.login(new LoginRequest("sv@unihub.edu.vn", "Secret@123")))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.TOO_MANY_REQUESTS);
	}

	@Test
	void refreshRotatesTokenAndRevokesOldOne() {
		UserEntity user = sampleUser("gv@unihub.edu.vn", "Secret@123", "LECTURER");
		RefreshTokenEntity existing = new RefreshTokenEntity();
		existing.setUserId(user.getId());
		existing.setExpiresAt(Instant.now().plusSeconds(600));
		existing.setRevoked(false);

		when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(existing));
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		AuthResponse rotated = authService.refresh(new RefreshTokenRequest("raw-token-1"));

		assertThat(existing.isRevoked()).isTrue();
		assertThat(rotated.accessToken()).isNotBlank();
		assertThat(rotated.refreshToken()).isNotBlank();
	}

	@Test
	void refreshRevokesAllTokensOnRevokedTokenReuse() {
		UUID userId = UUID.randomUUID();
		RefreshTokenEntity revoked = new RefreshTokenEntity();
		revoked.setUserId(userId);
		revoked.setExpiresAt(Instant.now().plusSeconds(600));
		revoked.setRevoked(true);

		when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(revoked));

		assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("reused-token")))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.UNAUTHORIZED);
		verify(refreshTokenRepository).revokeAllActiveByUserId(userId);
	}

	@Test
	void logoutRevokesRefreshToken() {
		RefreshTokenEntity token = new RefreshTokenEntity();
		token.setRevoked(false);
		when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

		authService.logout(new LogoutRequest("some-token"));

		assertThat(token.isRevoked()).isTrue();
		verify(refreshTokenRepository).save(token);
	}

	@Test
	void changePasswordUpdatesHashAndRevokesSessions() {
		UserEntity user = sampleUser("admin@unihub.edu.vn", "OldPass@123", "ADMIN");
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		authService.changePassword(user.getId(), new ChangePasswordRequest("OldPass@123", "NewPass@123"));

		assertThat(passwordEncoder.matches("NewPass@123", user.getPasswordHash())).isTrue();
		verify(refreshTokenRepository).revokeAllActiveByUserId(user.getId());
	}

	@Test
	void forgotAndResetPasswordFlowsWorkEndToEnd() {
		UserEntity user = sampleUser("sv@unihub.edu.vn", "OldPass@123", "STUDENT");
		when(userRepository.findByEmailIgnoreCase("sv@unihub.edu.vn")).thenReturn(Optional.of(user));

		authService.forgotPassword(new ForgotPasswordRequest("sv@unihub.edu.vn"));
		verify(passwordResetTokenRepository).save(any());
		verify(emailService).sendPasswordResetEmail(eq("sv@unihub.edu.vn"), eq("Test User"), any());

		PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
		resetToken.setUserId(user.getId());
		resetToken.setExpiresAt(Instant.now().plusSeconds(300));
		resetToken.setUsed(false);
		when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(resetToken));
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		authService.resetPassword(new ResetPasswordRequest("raw-reset", "ResetPass@123"));

		assertThat(resetToken.isUsed()).isTrue();
		assertThat(passwordEncoder.matches("ResetPass@123", user.getPasswordHash())).isTrue();
		verify(refreshTokenRepository).revokeAllActiveByUserId(user.getId());
	}

	private UserEntity sampleUser(String email, String rawPassword, String roleName) {
		RoleEntity role = new RoleEntity();
		role.setName(roleName);

		UserEntity user = new UserEntity();
		user.setId(UUID.randomUUID());
		user.setEmail(email);
		user.setFullName("Test User");
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(Set.of(role));
		return user;
	}

	@Test
	void loginWithGoogleEmailSucceedsWhenAccountExists() {
		UserEntity user = sampleUser("sv@unihub.edu.vn", "Secret@123", "STUDENT");
		when(userRepository.findByEmailIgnoreCase("sv@unihub.edu.vn")).thenReturn(Optional.of(user));

		AuthResponse response = authService.loginWithGoogleEmail("sv@unihub.edu.vn");

		assertThat(response.accessToken()).isNotBlank();
		assertThat(response.user().email()).isEqualTo("sv@unihub.edu.vn");
	}

	@Test
	void loginWithGoogleEmailRejectsUnregisteredEmail() {
		when(userRepository.findByEmailIgnoreCase("unknown@gmail.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.loginWithGoogleEmail("unknown@gmail.com")).isInstanceOf(AppException.class)
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
	}
}
