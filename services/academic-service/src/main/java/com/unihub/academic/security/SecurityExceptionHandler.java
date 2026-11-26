package com.unihub.academic.security;

import com.unihub.common.api.ErrorResponse;
import com.unihub.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		ErrorCode code = ErrorCode.FORBIDDEN;
		return ResponseEntity.status(code.getStatus())
				.body(ErrorResponse.of(code.getCode(), code.getDefaultMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
		ErrorCode code = ErrorCode.UNAUTHORIZED;
		return ResponseEntity.status(code.getStatus())
				.body(ErrorResponse.of(code.getCode(), code.getDefaultMessage(), request.getRequestURI()));
	}
}
