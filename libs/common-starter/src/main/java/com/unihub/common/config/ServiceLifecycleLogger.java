package com.unihub.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

/**
 * Logs concise Vietnamese lifecycle messages when a service starts or stops.
 */
public class ServiceLifecycleLogger {

	private static final Logger log = LoggerFactory.getLogger(ServiceLifecycleLogger.class);

	private final String serviceName;
	private final String port;

	public ServiceLifecycleLogger(@Value("${spring.application.name:unihub-service}") String serviceName,
			@Value("${server.port:8080}") String port) {
		this.serviceName = serviceName;
		this.port = port;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		log.info("[{}] Đã khởi động thành công — đang chạy trên cổng {}", serviceName, port);
	}

	@EventListener(ContextClosedEvent.class)
	public void onStop() {
		log.info("[{}] Đã tắt service", serviceName);
	}
}
