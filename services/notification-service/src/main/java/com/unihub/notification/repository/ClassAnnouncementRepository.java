package com.unihub.notification.repository;

import com.unihub.notification.domain.ClassAnnouncementEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassAnnouncementRepository extends JpaRepository<ClassAnnouncementEntity, UUID> {
	List<ClassAnnouncementEntity> findByCourseSectionIdOrderByPinnedDescCreatedAtDesc(UUID courseSectionId);
}
