package com.unihub.identity.dto;

import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.domain.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserDto(UUID id, String email, String fullName, String code, UserStatus status, UUID departmentId,
		UUID adminClassId, List<String> roles, Instant createdAt, Instant updatedAt) {

	public static UserDto from(UserEntity entity) {
		List<String> roleNames = entity.getRoles().stream().map(RoleEntity::getName).sorted().toList();
		return new UserDto(entity.getId(), entity.getEmail(), entity.getFullName(), entity.getCode(),
				entity.getStatus(), entity.getDepartmentId(), entity.getAdminClassId(), roleNames,
				entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
