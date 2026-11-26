package com.unihub.learning.repository;

import com.unihub.learning.domain.LessonProgressEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonProgressRepository
		extends
			JpaRepository<LessonProgressEntity, LessonProgressEntity.ProgressKey> {
	List<LessonProgressEntity> findByStudentIdAndLessonIdIn(UUID studentId, List<UUID> lessonIds);
}
