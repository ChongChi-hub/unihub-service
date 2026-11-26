package com.unihub.common.exception;

import com.unihub.common.api.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized REST exception handler producing uniform ErrorResponse payloads.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
		ErrorCode errorCode = ex.getErrorCode();
		ErrorResponse body = ErrorResponse.of(errorCode.getCode(), ex.getMessage(), request.getRequestURI());
		return ResponseEntity.status(errorCode.getStatus()).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
		ErrorResponse body = ErrorResponse.of(errorCode.getCode(), errorCode.getDefaultMessage(), fieldErrors,
				request.getRequestURI());
		return ResponseEntity.status(errorCode.getStatus()).body(body);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getConstraintViolations()
				.forEach(v -> fieldErrors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
		ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
		ErrorResponse body = ErrorResponse.of(errorCode.getCode(), errorCode.getDefaultMessage(), fieldErrors,
				request.getRequestURI());
		return ResponseEntity.status(errorCode.getStatus()).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception at {}", request.getRequestURI(), ex);
		ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
		ErrorResponse body = ErrorResponse.of(errorCode.getCode(), errorCode.getDefaultMessage(),
				request.getRequestURI());
		return ResponseEntity.status(errorCode.getStatus()).body(body);
	}
}
