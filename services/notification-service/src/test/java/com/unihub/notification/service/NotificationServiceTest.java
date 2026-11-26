package com.unihub.notification.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unihub.common.exception.AppException;
import com.unihub.notification.domain.NotificationEntity;
import com.unihub.notification.dto.NotificationDtos.NotificationEventPayload;
import com.unihub.notification.repository.ClassAnnouncementRepository;
import com.unihub.notification.repository.NotificationRepository;
import com.unihub.notification.repository.ProcessedEventRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private ClassAnnouncementRepository announcementRepository;
	@Mock
	private ProcessedEventRepository processedEventRepository;

	private NotificationService notificationService;

	@BeforeEach
	void setUp() {
		notificationService = new NotificationService(notificationRepository, announcementRepository,
				processedEventRepository);
	}

	@Test
	void processEventIdempotentlySkipsDuplicateEvents() {
		UUID eventId = UUID.randomUUID();
		NotificationEventPayload payload = new NotificationEventPayload(eventId, "ASSIGNMENT_GRADED", UUID.randomUUID(),
				"Graded", "Score: 9.5", "ASSIGNMENT", UUID.randomUUID());

		when(processedEventRepository.existsById(eventId)).thenReturn(true);

		boolean processed = notificationService.processEventIdempotently(payload);
		assertFalse(processed);
		verify(notificationRepository, never()).save(any());
	}

	@Test
	void processEventIdempotentlyCreatesNotificationOnce() {
		UUID eventId = UUID.randomUUID();
		UUID recipientId = UUID.randomUUID();
		NotificationEventPayload payload = new NotificationEventPayload(eventId, "ASSIGNMENT_GRADED", recipientId,
				"Graded", "Score: 9.5", "ASSIGNMENT", UUID.randomUUID());

		when(processedEventRepository.existsById(eventId)).thenReturn(false);
		when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		boolean processed = notificationService.processEventIdempotently(payload);
		assertTrue(processed);
		verify(processedEventRepository).save(any());
		verify(notificationRepository).save(any());
	}

	@Test
	void markAsReadRejectsDifferentUser() {
		UUID notifId = UUID.randomUUID();
		NotificationEntity n = new NotificationEntity();
		n.setId(notifId);
		n.setRecipientId(UUID.randomUUID());
		when(notificationRepository.findById(notifId)).thenReturn(Optional.of(n));

		assertThrows(AppException.class, () -> notificationService.markAsRead(notifId, UUID.randomUUID()));
	}
}
