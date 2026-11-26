package com.unihub.notification.repository;

import com.unihub.notification.domain.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
	List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

	long countByRecipientIdAndReadFalse(UUID recipientId);
}
