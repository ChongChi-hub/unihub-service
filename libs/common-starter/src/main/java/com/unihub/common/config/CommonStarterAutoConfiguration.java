package com.unihub.common.config;

import com.unihub.common.exception.GlobalExceptionHandler;
import com.unihub.common.web.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/** Auto-configuration registering shared web filters and exception handling. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonStarterAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public GlobalExceptionHandler globalExceptionHandler() {
		return new GlobalExceptionHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	public CorrelationIdFilter correlationIdFilter() {
		return new CorrelationIdFilter();
	}
}
