package com.unihub.common.exception;

import org.springframework.http.HttpStatus;

/** Standard error codes shared across all Unihub microservices. */
public enum ErrorCode {
	INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"), VALIDATION_ERROR(
			"VALIDATION_ERROR", HttpStatus.BAD_REQUEST,
			"Validation failed"), BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request"), UNAUTHORIZED(
					"UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"), FORBIDDEN("FORBIDDEN",
							HttpStatus.FORBIDDEN, "Access denied"), NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND,
									"Resource not found"), CONFLICT("CONFLICT", HttpStatus.CONFLICT,
											"Resource conflict"), TOO_MANY_REQUESTS("TOO_MANY_REQUESTS",
													HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");

	private final String code;
	private final HttpStatus status;
	private final String defaultMessage;

	ErrorCode(String code, HttpStatus status, String defaultMessage) {
		this.code = code;
		this.status = status;
		this.defaultMessage = defaultMessage;
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}
}
