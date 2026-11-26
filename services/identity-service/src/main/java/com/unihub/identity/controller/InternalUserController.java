package com.unihub.identity.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.domain.UserStatus;
import com.unihub.identity.dto.UserDto;
import com.unihub.identity.repository.UserRepository;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal read endpoint for service-to-service user lookups (e.g.
 * academic-service via Feign).
 */
@RestController
@RequestMapping("/api/v1/internal/users")
public class InternalUserController {

	private final UserRepository userRepository;

	public InternalUserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/{id}")
	public ApiResponse<UserDto> getUserSummary(@PathVariable UUID id) {
		UserEntity user = userRepository.findById(id).filter(u -> u.getStatus() != UserStatus.DELETED)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
		return ApiResponse.ok(UserDto.from(user));
	}
}
