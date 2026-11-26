package com.unihub.assessment.repository;

import com.unihub.assessment.domain.QuizAnswerEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswerEntity, QuizAnswerEntity.QuizAnswerId> {
	List<QuizAnswerEntity> findByAttemptId(UUID attemptId);
}
