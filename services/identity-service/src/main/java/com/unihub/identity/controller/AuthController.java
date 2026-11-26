package com.unihub.identity.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.identity.dto.AuthResponse;
import com.unihub.identity.dto.ChangePasswordRequest;
import com.unihub.identity.dto.ForgotPasswordRequest;
import com.unihub.identity.dto.LoginRequest;
import com.unihub.identity.dto.LogoutRequest;
import com.unihub.identity.dto.RefreshTokenRequest;
import com.unihub.identity.dto.ResetPasswordRequest;
import com.unihub.identity.security.GatewayUserPrincipal;
import com.unihub.identity.security.JwtService;
import com.unihub.identity.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

	@PostMapping("/refresh")
	public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ApiResponse.ok("Token refreshed", authService.refresh(request));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
		authService.logout(request);
		return ApiResponse.okMessage("Logged out successfully");
	}

	@PostMapping("/change-password")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> changePassword(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@Valid @RequestBody ChangePasswordRequest request) {
		authService.changePassword(principal.userId(), request);
		return ApiResponse.okMessage("Password changed successfully");
	}

	@PostMapping("/forgot-password")
	public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ApiResponse.okMessage("If the email exists, a password reset link has been sent");
	}

	@PostMapping("/reset-password")
	public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ApiResponse.okMessage("Password has been reset successfully");
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<GatewayUserPrincipal> me(@AuthenticationPrincipal GatewayUserPrincipal principal) {
		return ApiResponse.ok(principal);
	}

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.okMessage("Logged out successfully");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal GatewayUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.userId(), request);
        return ApiResponse.okMessage("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.okMessage(
                "If the email exists, a password reset link has been sent");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.okMessage("Password has been reset successfully");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GatewayUserPrincipal> me(
            @AuthenticationPrincipal GatewayUserPrincipal principal) {
        return ApiResponse.ok(principal);
    }

    @GetMapping("/public-key")
    public ApiResponse<Map<String, String>> publicKey() {
        return ApiResponse.ok(
                Map.of("algorithm", "RS256", "publicKey", jwtService.getPublicKeyBase64()));
    }
}
