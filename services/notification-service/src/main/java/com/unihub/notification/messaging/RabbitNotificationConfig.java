package com.unihub.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitNotificationConfig {

	public static final String EVENTS_EXCHANGE = "unihub.events";
	public static final String NOTIFICATION_QUEUE = "notification.events";

	@Bean
	public TopicExchange unihubEventsExchange() {
		return new TopicExchange(EVENTS_EXCHANGE, true, false);
	}

	@Bean
	public Queue notificationEventsQueue() {
		return new Queue(NOTIFICATION_QUEUE, true);
	}

	@Bean
	public Binding notificationEventsBinding(Queue notificationEventsQueue, TopicExchange unihubEventsExchange) {
		return BindingBuilder.bind(notificationEventsQueue).to(unihubEventsExchange).with("notification.#");
	}

	@Bean
	public MessageConverter jacksonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
