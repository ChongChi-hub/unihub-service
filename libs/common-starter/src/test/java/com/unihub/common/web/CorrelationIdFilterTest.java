package com.unihub.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.unihub.common.api.CorrelationIdConstants;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

	private final CorrelationIdFilter filter = new CorrelationIdFilter();

	@Test
	void generatesCorrelationIdWhenMissingAndCleansMdc() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = (req, res) -> assertThat(MDC.get(CorrelationIdConstants.MDC_KEY)).isNotBlank();

		filter.doFilter(request, response, chain);

		assertThat(response.getHeader(CorrelationIdConstants.HEADER_NAME)).isNotBlank();
		assertThat(MDC.get(CorrelationIdConstants.MDC_KEY)).isNull();
	}

	@Test
	void propagatesIncomingCorrelationId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdConstants.HEADER_NAME, "req-abc-123");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = (req, res) -> assertThat(MDC.get(CorrelationIdConstants.MDC_KEY)).isEqualTo("req-abc-123");

		filter.doFilter(request, response, chain);

		assertThat(response.getHeader(CorrelationIdConstants.HEADER_NAME)).isEqualTo("req-abc-123");
	}
}
