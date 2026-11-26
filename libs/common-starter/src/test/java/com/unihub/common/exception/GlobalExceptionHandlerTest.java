package com.unihub.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.unihub.common.api.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void handleAppExceptionReturnsMappedStatusAndCode() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/123");
		AppException ex = new AppException(ErrorCode.NOT_FOUND, "User not found");

		ResponseEntity<ErrorResponse> response = handler.handleAppException(ex, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().success()).isFalse();
		assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
		assertThat(response.getBody().message()).isEqualTo("User not found");
		assertThat(response.getBody().path()).isEqualTo("/api/v1/users/123");
	}

	@Test
	void handleUnexpectedReturnsInternalServerErrorWithoutLeakingDetails() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");

		ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("DB password leaked"),
				request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
		assertThat(response.getBody().message()).isEqualTo("Internal server error");
	}
}
