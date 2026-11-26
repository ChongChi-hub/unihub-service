package com.unihub.common.feign;

import com.unihub.common.api.CorrelationIdConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propagates correlation ID, Authorization JWT, and gateway-forwarded X-User-*
 * headers across synchronous OpenFeign service-to-service calls.
 */
public class FeignAuthInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		String correlationId = MDC.get(CorrelationIdConstants.MDC_KEY);
		if (StringUtils.hasText(correlationId)) {
			template.header(CorrelationIdConstants.HEADER_NAME, correlationId);
		}

		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
			HttpServletRequest request = attrs.getRequest();
			forwardHeader(request, template, HttpHeaders.AUTHORIZATION);
			forwardHeader(request, template, "X-User-Id");
			forwardHeader(request, template, "X-User-Email");
			forwardHeader(request, template, "X-User-Roles");
		}
	}

	private static void forwardHeader(HttpServletRequest request, RequestTemplate template, String name) {
		String value = request.getHeader(name);
		if (StringUtils.hasText(value)) {
			template.header(name, value);
		}
	}
}
