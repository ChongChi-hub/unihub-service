package com.unihub.common.config;

import com.unihub.common.feign.FeignAuthInterceptor;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignCommonAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public FeignAuthInterceptor feignAuthInterceptor() {
		return new FeignAuthInterceptor();
	}
}
