package com.unihub.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateUserRequest(
		@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
		@NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be at least 8 characters") String password,
		@NotBlank(message = "Full name is required") @Size(max = 255) String fullName, @Size(max = 64) String code,
		UUID departmentId, UUID adminClassId, @NotEmpty(message = "At least one role is required") Set<String> roles) {
}
