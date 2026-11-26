package com.unihub.assessment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.assessment.domain.QuestionEntity;
import com.unihub.assessment.domain.QuizAttemptEntity;
import com.unihub.assessment.domain.QuizEntity;
import com.unihub.assessment.domain.QuizQuestionEntity;
import com.unihub.assessment.dto.AssessmentDtos.AnswerInput;
import com.unihub.assessment.dto.AssessmentDtos.QuizAttemptDto;
import com.unihub.assessment.dto.AssessmentDtos.SubmitQuizRequest;
import com.unihub.assessment.repository.QuestionRepository;
import com.unihub.assessment.repository.QuizAnswerRepository;
import com.unihub.assessment.repository.QuizAttemptRepository;
import com.unihub.assessment.repository.QuizQuestionRepository;
import com.unihub.assessment.repository.QuizRepository;
import com.unihub.common.exception.AppException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

	@Mock
	private QuestionRepository questionRepository;
	@Mock
	private QuizRepository quizRepository;
	@Mock
	private QuizQuestionRepository quizQuestionRepository;
	@Mock
	private QuizAttemptRepository quizAttemptRepository;
	@Mock
	private QuizAnswerRepository quizAnswerRepository;

	private QuizService quizService;

	@BeforeEach
	void setUp() {
		quizService = new QuizService(questionRepository, quizRepository, quizQuestionRepository, quizAttemptRepository,
				quizAnswerRepository, new ObjectMapper());
	}

	@Test
	void submitAttemptAutoGradesCorrectAnswers() {
		UUID quizId = UUID.randomUUID();
		UUID attemptId = UUID.randomUUID();
		UUID studentId = UUID.randomUUID();
		UUID q1Id = UUID.randomUUID();
		UUID q2Id = UUID.randomUUID();

		QuizAttemptEntity attempt = new QuizAttemptEntity();
		attempt.setId(attemptId);
		attempt.setQuizId(quizId);
		attempt.setStudentId(studentId);
		attempt.setStatus("IN_PROGRESS");

		QuizEntity quiz = new QuizEntity();
		quiz.setId(quizId);
		quiz.setShowCorrectAnswers(true);

		QuizQuestionEntity l1 = new QuizQuestionEntity();
		l1.setQuizId(quizId);
		l1.setQuestionId(q1Id);
		l1.setPoints(new BigDecimal("2.50"));

		QuizQuestionEntity l2 = new QuizQuestionEntity();
		l2.setQuizId(quizId);
		l2.setQuestionId(q2Id);
		l2.setPoints(new BigDecimal("2.50"));

		QuestionEntity q1 = new QuestionEntity();
		q1.setId(q1Id);
		q1.setCorrectAnswersJson("[\"A\"]");

		QuestionEntity q2 = new QuestionEntity();
		q2.setId(q2Id);
		q2.setCorrectAnswersJson("[\"B\",\"C\"]");

		when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
		when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
		when(quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quizId)).thenReturn(List.of(l1, l2));
		when(questionRepository.findAllById(any())).thenReturn(List.of(q1, q2));
		when(quizAnswerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		QuizAttemptDto result = quizService.submitAttempt(attemptId, studentId, new SubmitQuizRequest(
				List.of(new AnswerInput(q1Id, List.of("A")), new AnswerInput(q2Id, List.of("B")))));

		assertEquals("GRADED", result.status());
		assertEquals(new BigDecimal("2.50"), result.score());
	}

	@Test
	void startAttemptRejectsWhenMaxAttemptsReached() {
		UUID quizId = UUID.randomUUID();
		UUID studentId = UUID.randomUUID();

		QuizEntity quiz = new QuizEntity();
		quiz.setId(quizId);
		quiz.setPublished(true);
		quiz.setMaxAttempts(1);

		when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
		when(quizAttemptRepository.countByQuizIdAndStudentId(quizId, studentId)).thenReturn(1L);

		assertThrows(AppException.class, () -> quizService.startAttempt(quizId, studentId));
	}
}
