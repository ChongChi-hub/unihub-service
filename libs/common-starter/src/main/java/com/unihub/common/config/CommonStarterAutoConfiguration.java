package com.unihub.common.config;

import com.unihub.common.exception.GlobalExceptionHandler;
import com.unihub.common.web.CorrelationIdFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration registering shared web filters, exception handling, and
 * lifecycle logs.
 */
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

	@Bean
	@ConditionalOnMissingBean
	public ServiceLifecycleLogger serviceLifecycleLogger(
			@Value("${spring.application.name:unihub-service}") String serviceName,
			@Value("${server.port:8080}") String port) {
		return new ServiceLifecycleLogger(serviceName, port);
	}
}
