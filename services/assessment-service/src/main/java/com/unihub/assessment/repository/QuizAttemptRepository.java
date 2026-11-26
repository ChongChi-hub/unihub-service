package com.unihub.assessment.repository;

import com.unihub.assessment.domain.QuizAttemptEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttemptEntity, UUID> {
	List<QuizAttemptEntity> findByQuizIdAndStudentIdOrderByAttemptNumberDesc(UUID quizId, UUID studentId);

	long countByQuizIdAndStudentId(UUID quizId, UUID studentId);
}
