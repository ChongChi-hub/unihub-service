package com.unihub.learning.repository;

import com.unihub.learning.domain.LessonCommentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonCommentRepository extends JpaRepository<LessonCommentEntity, UUID> {
	List<LessonCommentEntity> findByLessonIdOrderByCreatedAtAsc(UUID lessonId);
}
