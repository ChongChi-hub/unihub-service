package com.unihub.assessment.repository;

import com.unihub.assessment.domain.QuestionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {
	List<QuestionEntity> findBySubjectIdOrderByCreatedAtDesc(UUID subjectId);
}
