package com.unihub.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UpdateUserRequest(@NotBlank(message = "Full name is required") @Size(max = 255) String fullName,
		@Size(max = 64) String code, UUID departmentId, UUID adminClassId,
		@NotEmpty(message = "At least one role is required") Set<String> roles) {
}
