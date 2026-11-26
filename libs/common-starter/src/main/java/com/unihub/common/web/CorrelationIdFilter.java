package com.unihub.common.web;

import com.unihub.common.api.CorrelationIdConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts or generates X-Correlation-Id, stores it in SLF4J MDC for logging,
 * and echoes it back on the HTTP response.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String correlationId = request.getHeader(CorrelationIdConstants.HEADER_NAME);
		if (!StringUtils.hasText(correlationId)) {
			correlationId = UUID.randomUUID().toString();
		}
		MDC.put(CorrelationIdConstants.MDC_KEY, correlationId);
		response.setHeader(CorrelationIdConstants.HEADER_NAME, correlationId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(CorrelationIdConstants.MDC_KEY);
		}
	}
}
