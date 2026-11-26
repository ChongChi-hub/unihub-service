package com.unihub.assessment.repository;

import com.unihub.assessment.domain.QuizEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<QuizEntity, UUID> {
	List<QuizEntity> findByCourseSectionIdOrderByCreatedAtDesc(UUID courseSectionId);
}
