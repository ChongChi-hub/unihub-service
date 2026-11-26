package com.unihub.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;

/** Standard error payload returned by GlobalExceptionHandler. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(boolean success, String code, String message, Map<String, String> fieldErrors, String path,
		String correlationId, Instant timestamp) {

	public static ErrorResponse of(String code, String message, String path) {
		return of(code, message, null, path);
	}

	public static ErrorResponse of(String code, String message, Map<String, String> fieldErrors, String path) {
		return new ErrorResponse(false, code, message, fieldErrors, path, MDC.get(CorrelationIdConstants.MDC_KEY),
				Instant.now());
	}
}
