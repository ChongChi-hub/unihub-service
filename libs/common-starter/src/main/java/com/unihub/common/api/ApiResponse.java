package com.unihub.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import org.slf4j.MDC;

/** Standard API envelope for successful responses. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data, String correlationId, Instant timestamp) {

	public static <T> ApiResponse<T> ok(T data) {
		return ok("Success", data);
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(true, message, data, MDC.get(CorrelationIdConstants.MDC_KEY), Instant.now());
	}

	public static ApiResponse<Void> okMessage(String message) {
		return ok(message, null);
	}
}
