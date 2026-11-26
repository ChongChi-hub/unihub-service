package com.unihub.identity.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.common.api.PageResponse;
import com.unihub.identity.domain.UserStatus;
import com.unihub.identity.dto.CreateUserRequest;
import com.unihub.identity.dto.UpdateUserRequest;
import com.unihub.identity.dto.UserDto;
import com.unihub.identity.security.GatewayUserPrincipal;
import com.unihub.identity.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping
	public ApiResponse<PageResponse<UserDto>> list(@RequestParam(required = false) String query,
			@RequestParam(required = false) String role, @RequestParam(required = false) UserStatus status,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ApiResponse.ok(adminUserService.list(query, role, status, pageable));
	}

	@GetMapping("/{id}")
	public ApiResponse<UserDto> getById(@PathVariable UUID id) {
		return ApiResponse.ok(adminUserService.getById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<UserDto> create(@AuthenticationPrincipal GatewayUserPrincipal actor,
			@Valid @RequestBody CreateUserRequest request) {
		return ApiResponse.ok("User created", adminUserService.create(actor, request));
	}

	@PutMapping("/{id}")
	public ApiResponse<UserDto> update(@AuthenticationPrincipal GatewayUserPrincipal actor, @PathVariable UUID id,
			@Valid @RequestBody UpdateUserRequest request) {
		return ApiResponse.ok("User updated", adminUserService.update(actor, id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> softDelete(@AuthenticationPrincipal GatewayUserPrincipal actor, @PathVariable UUID id) {
		adminUserService.softDelete(actor, id);
		return ApiResponse.okMessage("User deleted");
	}

	@PatchMapping("/{id}/lock")
	public ApiResponse<UserDto> lock(@AuthenticationPrincipal GatewayUserPrincipal actor, @PathVariable UUID id) {
		return ApiResponse.ok("User locked", adminUserService.lock(actor, id));
	}

	@PatchMapping("/{id}/unlock")
	public ApiResponse<UserDto> unlock(@AuthenticationPrincipal GatewayUserPrincipal actor, @PathVariable UUID id) {
		return ApiResponse.ok("User unlocked", adminUserService.unlock(actor, id));
	}

	@PostMapping("/{id}/reset-password")
	public ApiResponse<java.util.Map<String, String>> resetPassword(@AuthenticationPrincipal GatewayUserPrincipal actor,
			@PathVariable UUID id) {
		String tempPassword = adminUserService.resetPasswordByAdmin(actor, id);
		return ApiResponse.ok("Password reset", java.util.Map.of("temporaryPassword", tempPassword));
	}
}
