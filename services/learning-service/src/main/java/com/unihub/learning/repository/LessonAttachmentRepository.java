package com.unihub.learning.repository;

import com.unihub.learning.domain.LessonAttachmentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonAttachmentRepository extends JpaRepository<LessonAttachmentEntity, UUID> {
	List<LessonAttachmentEntity> findByLessonId(UUID lessonId);
}
