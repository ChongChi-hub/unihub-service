package com.unihub.common.feign;

import static org.assertj.core.api.Assertions.assertThat;

import com.unihub.common.api.CorrelationIdConstants;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class FeignAuthInterceptorTest {

	@AfterEach
	void cleanup() {
		MDC.clear();
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void propagatesCorrelationIdJwtAndUserHeaders() {
		MDC.put(CorrelationIdConstants.MDC_KEY, "corr-123");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token");
		request.addHeader("X-User-Id", "user-1");
		request.addHeader("X-User-Roles", "ADMIN");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		RequestTemplate template = new RequestTemplate();
		new FeignAuthInterceptor().apply(template);

		assertThat(template.headers().get(CorrelationIdConstants.HEADER_NAME)).containsExactly("corr-123");
		assertThat(template.headers().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer jwt-token");
		assertThat(template.headers().get("X-User-Id")).containsExactly("user-1");
		assertThat(template.headers().get("X-User-Roles")).containsExactly("ADMIN");
	}
}
