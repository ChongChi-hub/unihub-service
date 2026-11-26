package com.unihub.common.api;

/** Shared constants for distributed correlation ID propagation. */
public final class CorrelationIdConstants {

	public static final String HEADER_NAME = "X-Correlation-Id";
	public static final String MDC_KEY = "correlationId";

	private CorrelationIdConstants() {
	}
}
