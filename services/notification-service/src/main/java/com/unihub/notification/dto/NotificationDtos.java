package com.unihub.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class NotificationDtos {

	private NotificationDtos() {
	}

	public record NotificationDto(UUID id, UUID recipientId, String type, String title, String body,
			String referenceType, UUID referenceId, boolean read, Instant createdAt) {
	}

	public record CreateNotificationRequest(@NotNull UUID recipientId, @NotBlank String type, @NotBlank String title,
			@NotBlank String body, String referenceType, UUID referenceId) {
	}

	public record UnreadCountDto(long unreadCount) {
	}

	public record AnnouncementRequest(@NotNull UUID courseSectionId, @NotBlank String title, @NotBlank String content,
			boolean pinned) {
	}

	public record AnnouncementDto(UUID id, UUID courseSectionId, UUID authorId, String title, String content,
			boolean pinned, Instant createdAt, Instant updatedAt) {
	}

	public record NotificationEventPayload(UUID eventId, String eventType, UUID recipientId, String title, String body,
			String referenceType, UUID referenceId) {
	}
}
