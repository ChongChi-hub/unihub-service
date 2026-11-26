package com.unihub.notification.messaging;

import com.unihub.notification.dto.NotificationDtos.NotificationEventPayload;
import com.unihub.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

	private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
	private final NotificationService notificationService;

	public NotificationEventListener(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@RabbitListener(queues = RabbitNotificationConfig.NOTIFICATION_QUEUE)
	public void onNotificationEvent(NotificationEventPayload payload) {
		boolean processed = notificationService.processEventIdempotently(payload);
		if (!processed) {
			log.debug("Skipping duplicate notification event {}", payload.eventId());
		}
	}
}
