package com.unihub.academic.client;

import com.unihub.common.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service", url = "${app.services.identity-url:http://localhost:8081}", fallback = IdentityClientFallback.class)
public interface IdentityClient {

	@GetMapping("/api/v1/internal/users/{id}")
	ApiResponse<RemoteUserDto> getUserById(@PathVariable UUID id);

	record RemoteUserDto(UUID id, String email, String fullName, String code, String status, List<String> roles) {
	}
}
