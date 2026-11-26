package com.unihub.identity.service;

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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final Duration PASSWORD_RESET_TTL = Duration.ofMinutes(15);

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final LoginRateLimiter rateLimiter;
	private final EmailService emailService;

	public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
			PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder,
			JwtService jwtService, JwtProperties jwtProperties, LoginRateLimiter rateLimiter,
			EmailService emailService) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
		this.rateLimiter = rateLimiter;
		this.emailService = emailService;
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		String normalizedEmail = request.email().trim().toLowerCase();
		if (rateLimiter.isBlocked(normalizedEmail)) {
			throw new AppException(ErrorCode.TOO_MANY_REQUESTS,
					"Too many failed login attempts. Please try again later.");
		}

		UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseThrow(() -> {
			rateLimiter.recordFailure(normalizedEmail);
			return new AppException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
		});

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new AppException(ErrorCode.FORBIDDEN, "Account is locked or inactive");
		}

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			rateLimiter.recordFailure(normalizedEmail);
			throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
		}

		rateLimiter.reset(normalizedEmail);
		return issueTokenPair(user);
	}

	@Transactional
	public AuthResponse refresh(RefreshTokenRequest request) {
		RefreshTokenEntity existing = refreshTokenRepository.findByTokenHash(sha256Hex(request.refreshToken()))
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

		if (existing.isRevoked()) {
			// Reuse of a rotated token indicates possible theft: revoke all active
			// sessions.
			refreshTokenRepository.revokeAllActiveByUserId(existing.getUserId());
			throw new AppException(ErrorCode.UNAUTHORIZED, "Refresh token has been revoked");
		}

		if (existing.getExpiresAt().isBefore(Instant.now())) {
			existing.setRevoked(true);
			refreshTokenRepository.save(existing);
			throw new AppException(ErrorCode.UNAUTHORIZED, "Refresh token has expired");
		}

		existing.setRevoked(true);
		refreshTokenRepository.save(existing);

		UserEntity user = userRepository.findById(existing.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "User not found"));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new AppException(ErrorCode.FORBIDDEN, "Account is locked or inactive");
		}

		return issueTokenPair(user);
	}

	@Transactional
	public void logout(LogoutRequest request) {
		refreshTokenRepository.findByTokenHash(sha256Hex(request.refreshToken())).ifPresent(token -> {
			token.setRevoked(true);
			refreshTokenRepository.save(token);
		});
	}

	@Transactional
	public void changePassword(UUID userId, ChangePasswordRequest request) {
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Current password is incorrect");
		}

		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
		refreshTokenRepository.revokeAllActiveByUserId(userId);
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {
		String normalizedEmail = request.email().trim().toLowerCase();
		userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
			if (user.getStatus() != UserStatus.ACTIVE) {
				return;
			}
			String rawToken = UUID.randomUUID().toString().replace("-", "");
			PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
			resetToken.setUserId(user.getId());
			resetToken.setTokenHash(sha256Hex(rawToken));
			resetToken.setExpiresAt(Instant.now().plus(PASSWORD_RESET_TTL));
			passwordResetTokenRepository.save(resetToken);

			emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), rawToken);
		});
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByTokenHash(sha256Hex(request.token()))
				.orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Invalid reset token"));

		if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Reset token has expired or been used");
		}

		UserEntity user = userRepository.findById(resetToken.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);

		resetToken.setUsed(true);
		passwordResetTokenRepository.save(resetToken);
		refreshTokenRepository.revokeAllActiveByUserId(user.getId());
	}

	@Transactional
	public AuthResponse loginWithGoogleEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new AppException(ErrorCode.UNAUTHORIZED, "Google account has no verified email");
		}
		String normalizedEmail = email.trim().toLowerCase();
		UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseThrow(
				() -> new AppException(ErrorCode.FORBIDDEN, "No Unihub account linked to this Google email"));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new AppException(ErrorCode.FORBIDDEN, "Account is locked or inactive");
		}
		return issueTokenPair(user);
	}

	private AuthResponse issueTokenPair(UserEntity user) {
		String accessToken = jwtService.generateAccessToken(user);
		String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();

		RefreshTokenEntity refreshToken = new RefreshTokenEntity();
		refreshToken.setUserId(user.getId());
		refreshToken.setTokenHash(sha256Hex(rawRefreshToken));
		refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.refreshExpirationMs()));
		refreshTokenRepository.save(refreshToken);

		List<String> roles = user.getRoles().stream().map(RoleEntity::getName).sorted().toList();
		AuthResponse.UserSummary summary = new AuthResponse.UserSummary(user.getId(), user.getEmail(),
				user.getFullName(), roles);

		return new AuthResponse(accessToken, rawRefreshToken, "Bearer", jwtProperties.accessExpirationMs(), summary);
	}

	private static String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 unavailable", ex);
		}
	}
}
