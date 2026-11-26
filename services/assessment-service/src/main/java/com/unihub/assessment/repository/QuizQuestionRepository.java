package com.unihub.assessment.repository;

import com.unihub.assessment.domain.QuizQuestionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestionEntity, QuizQuestionEntity.QuizQuestionId> {
	List<QuizQuestionEntity> findByQuizIdOrderBySortOrderAsc(UUID quizId);
}
