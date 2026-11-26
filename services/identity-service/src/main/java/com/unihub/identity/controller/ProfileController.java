package com.unihub.identity.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.repository.UserRepository;
import com.unihub.identity.security.GatewayUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-service profile management ([USR-003]); users cannot modify their own
 * roles.
 */
@RestController
@RequestMapping("/api/v1/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

	private final UserRepository userRepository;

	public ProfileController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public ApiResponse<ProfileDto> getProfile(@AuthenticationPrincipal GatewayUserPrincipal principal) {
		UserEntity user = userRepository.findById(principal.userId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
		return ApiResponse.ok(toDto(user));
	}

	@PutMapping
	@Transactional
	public ApiResponse<ProfileDto> updateProfile(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@Valid @RequestBody UpdateProfileRequest request) {
		UserEntity user = userRepository.findById(principal.userId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
		user.setFullName(request.fullName().trim());
		user.setAvatarFileId(request.avatarFileId());
		return ApiResponse.ok("Profile updated", toDto(userRepository.save(user)));
	}

	private static ProfileDto toDto(UserEntity user) {
		List<String> roles = user.getRoles().stream().map(RoleEntity::getName).sorted().toList();
		return new ProfileDto(user.getId(), user.getEmail(), user.getFullName(), user.getCode(), user.getAvatarFileId(),
				roles);
	}

	public record UpdateProfileRequest(@NotBlank(message = "Full name is required") @Size(max = 255) String fullName,
			@Size(max = 255) String avatarFileId) {
	}

	public record ProfileDto(UUID id, String email, String fullName, String code, String avatarFileId,
			List<String> roles) {
	}
}
