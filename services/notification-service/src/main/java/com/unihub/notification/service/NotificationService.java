package com.unihub.notification.service;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.notification.domain.ClassAnnouncementEntity;
import com.unihub.notification.domain.NotificationEntity;
import com.unihub.notification.domain.ProcessedEventEntity;
import com.unihub.notification.dto.NotificationDtos.AnnouncementDto;
import com.unihub.notification.dto.NotificationDtos.AnnouncementRequest;
import com.unihub.notification.dto.NotificationDtos.CreateNotificationRequest;
import com.unihub.notification.dto.NotificationDtos.NotificationDto;
import com.unihub.notification.dto.NotificationDtos.NotificationEventPayload;
import com.unihub.notification.dto.NotificationDtos.UnreadCountDto;
import com.unihub.notification.repository.ClassAnnouncementRepository;
import com.unihub.notification.repository.NotificationRepository;
import com.unihub.notification.repository.ProcessedEventRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final ClassAnnouncementRepository announcementRepository;
	private final ProcessedEventRepository processedEventRepository;

	public NotificationService(NotificationRepository notificationRepository,
			ClassAnnouncementRepository announcementRepository, ProcessedEventRepository processedEventRepository) {
		this.notificationRepository = notificationRepository;
		this.announcementRepository = announcementRepository;
		this.processedEventRepository = processedEventRepository;
	}

	@Transactional
	public NotificationDto createNotification(CreateNotificationRequest req) {
		NotificationEntity entity = new NotificationEntity();
		entity.setRecipientId(req.recipientId());
		entity.setType(req.type().trim().toUpperCase());
		entity.setTitle(req.title().trim());
		entity.setBody(req.body().trim());
		entity.setReferenceType(req.referenceType());
		entity.setReferenceId(req.referenceId());
		return toDto(notificationRepository.save(entity));
	}

	@Transactional
	public boolean processEventIdempotently(NotificationEventPayload payload) {
		if (payload.eventId() != null && processedEventRepository.existsById(payload.eventId())) {
			return false;
		}
		if (payload.eventId() != null) {
			ProcessedEventEntity evt = new ProcessedEventEntity();
			evt.setEventId(payload.eventId());
			evt.setEventType(payload.eventType() == null ? "NOTIFICATION_EVENT" : payload.eventType());
			processedEventRepository.save(evt);
		}
		if (payload.recipientId() != null) {
			createNotification(new CreateNotificationRequest(payload.recipientId(),
					payload.eventType() == null ? "SYSTEM" : payload.eventType(),
					payload.title() == null ? "Thông báo mới" : payload.title(),
					payload.body() == null ? "" : payload.body(), payload.referenceType(), payload.referenceId()));
		}
		return true;
	}

	@Transactional(readOnly = true)
	public List<NotificationDto> listMyNotifications(UUID userId) {
		return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public UnreadCountDto getUnreadCount(UUID userId) {
		return new UnreadCountDto(notificationRepository.countByRecipientIdAndReadFalse(userId));
	}

	@Transactional
	public NotificationDto markAsRead(UUID notificationId, UUID userId) {
		NotificationEntity entity = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Notification not found"));
		if (!entity.getRecipientId().equals(userId)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Cannot modify another user's notification");
		}
		entity.setRead(true);
		return toDto(notificationRepository.save(entity));
	}

	@Transactional
	public void markAllAsRead(UUID userId) {
		List<NotificationEntity> items = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
		items.stream().filter(n -> !n.isRead()).forEach(n -> n.setRead(true));
		notificationRepository.saveAll(items);
	}

	@Transactional
	public AnnouncementDto createAnnouncement(UUID authorId, AnnouncementRequest req) {
		ClassAnnouncementEntity entity = new ClassAnnouncementEntity();
		entity.setCourseSectionId(req.courseSectionId());
		entity.setAuthorId(authorId);
		entity.setTitle(req.title().trim());
		entity.setContent(req.content().trim());
		entity.setPinned(req.pinned());
		return toAnnouncementDto(announcementRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<AnnouncementDto> listAnnouncementsBySection(UUID courseSectionId) {
		return announcementRepository.findByCourseSectionIdOrderByPinnedDescCreatedAtDesc(courseSectionId).stream()
				.map(this::toAnnouncementDto).toList();
	}

	@Transactional
	public void deleteAnnouncement(UUID announcementId) {
		if (!announcementRepository.existsById(announcementId)) {
			throw new AppException(ErrorCode.NOT_FOUND, "Announcement not found");
		}
		announcementRepository.deleteById(announcementId);
	}

	private NotificationDto toDto(NotificationEntity n) {
		return new NotificationDto(n.getId(), n.getRecipientId(), n.getType(), n.getTitle(), n.getBody(),
				n.getReferenceType(), n.getReferenceId(), n.isRead(), n.getCreatedAt());
	}

	private AnnouncementDto toAnnouncementDto(ClassAnnouncementEntity a) {
		return new AnnouncementDto(a.getId(), a.getCourseSectionId(), a.getAuthorId(), a.getTitle(), a.getContent(),
				a.isPinned(), a.getCreatedAt(), a.getUpdatedAt());
	}
}
