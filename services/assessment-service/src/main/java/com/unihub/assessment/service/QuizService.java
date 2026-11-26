package com.unihub.assessment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.assessment.domain.QuestionEntity;
import com.unihub.assessment.domain.QuizAnswerEntity;
import com.unihub.assessment.domain.QuizAttemptEntity;
import com.unihub.assessment.domain.QuizEntity;
import com.unihub.assessment.domain.QuizQuestionEntity;
import com.unihub.assessment.dto.AssessmentDtos.AnswerInput;
import com.unihub.assessment.dto.AssessmentDtos.QuestionDto;
import com.unihub.assessment.dto.AssessmentDtos.QuestionRequest;
import com.unihub.assessment.dto.AssessmentDtos.QuizAnswerResultDto;
import com.unihub.assessment.dto.AssessmentDtos.QuizAttemptDto;
import com.unihub.assessment.dto.AssessmentDtos.QuizDto;
import com.unihub.assessment.dto.AssessmentDtos.QuizQuestionInput;
import com.unihub.assessment.dto.AssessmentDtos.QuizQuestionView;
import com.unihub.assessment.dto.AssessmentDtos.QuizRequest;
import com.unihub.assessment.dto.AssessmentDtos.SubmitQuizRequest;
import com.unihub.assessment.repository.QuestionRepository;
import com.unihub.assessment.repository.QuizAnswerRepository;
import com.unihub.assessment.repository.QuizAttemptRepository;
import com.unihub.assessment.repository.QuizQuestionRepository;
import com.unihub.assessment.repository.QuizRepository;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

	private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
	};

	private final QuestionRepository questionRepository;
	private final QuizRepository quizRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final QuizAttemptRepository quizAttemptRepository;
	private final QuizAnswerRepository quizAnswerRepository;
	private final ObjectMapper objectMapper;

	public QuizService(QuestionRepository questionRepository, QuizRepository quizRepository,
			QuizQuestionRepository quizQuestionRepository, QuizAttemptRepository quizAttemptRepository,
			QuizAnswerRepository quizAnswerRepository, ObjectMapper objectMapper) {
		this.questionRepository = questionRepository;
		this.quizRepository = quizRepository;
		this.quizQuestionRepository = quizQuestionRepository;
		this.quizAttemptRepository = quizAttemptRepository;
		this.quizAnswerRepository = quizAnswerRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public QuestionDto createQuestion(UUID lecturerId, QuestionRequest req) {
		QuestionEntity entity = new QuestionEntity();
		entity.setSubjectId(req.subjectId());
		entity.setCreatedBy(lecturerId);
		entity.setType(req.type().trim().toUpperCase());
		entity.setContent(req.content().trim());
		entity.setOptionsJson(toJson(req.options()));
		entity.setCorrectAnswersJson(toJson(req.correctAnswers()));
		entity.setDifficulty(req.difficulty() == null || req.difficulty().isBlank()
				? "MEDIUM"
				: req.difficulty().trim().toUpperCase());
		entity.setTopic(req.topic());
		entity.setTags(req.tags() == null ? null : String.join(",", req.tags()));
		return toQuestionDto(questionRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<QuestionDto> listQuestionsBySubject(UUID subjectId) {
		return questionRepository.findBySubjectIdOrderByCreatedAtDesc(subjectId).stream().map(this::toQuestionDto)
				.toList();
	}

	@Transactional
	public QuizDto createQuiz(QuizRequest req) {
		QuizEntity quiz = new QuizEntity();
		quiz.setCourseSectionId(req.courseSectionId());
		quiz.setTitle(req.title().trim());
		quiz.setDurationMinutes(req.durationMinutes());
		quiz.setMaxAttempts(req.maxAttempts());
		quiz.setShuffleQuestions(req.shuffleQuestions());
		quiz.setShowCorrectAnswers(req.showCorrectAnswers());
		quiz.setOpenAt(req.openAt());
		quiz.setCloseAt(req.closeAt());
		quiz.setPublished(req.published());
		QuizEntity saved = quizRepository.save(quiz);

		if (req.questions() != null && !req.questions().isEmpty()) {
			for (QuizQuestionInput item : req.questions()) {
				if (!questionRepository.existsById(item.questionId())) {
					throw new AppException(ErrorCode.NOT_FOUND, "Question not found: " + item.questionId());
				}
				QuizQuestionEntity link = new QuizQuestionEntity();
				link.setQuizId(saved.getId());
				link.setQuestionId(item.questionId());
				link.setSortOrder(item.sortOrder());
				link.setPoints(item.points());
				quizQuestionRepository.save(link);
			}
		}
		return getQuizDetail(saved.getId(), false);
	}

	@Transactional(readOnly = true)
	public List<QuizDto> listQuizzesBySection(UUID courseSectionId) {
		return quizRepository.findByCourseSectionIdOrderByCreatedAtDesc(courseSectionId).stream()
				.map(q -> getQuizDetail(q.getId(), false)).toList();
	}

	@Transactional(readOnly = true)
	public QuizDto getQuizDetail(UUID quizId, boolean applyShuffle) {
		QuizEntity quiz = quizRepository.findById(quizId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz not found"));
		List<QuizQuestionEntity> links = quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
		Map<UUID, QuestionEntity> questionMap = questionRepository
				.findAllById(links.stream().map(QuizQuestionEntity::getQuestionId).toList()).stream()
				.collect(Collectors.toMap(QuestionEntity::getId, Function.identity()));

		List<QuizQuestionView> views = links.stream().map(link -> {
			QuestionEntity q = questionMap.get(link.getQuestionId());
			return new QuizQuestionView(link.getQuestionId(), q.getType(), q.getContent(),
					fromJsonList(q.getOptionsJson()), link.getSortOrder(), link.getPoints());
		}).collect(Collectors.toList());

		if (applyShuffle && quiz.isShuffleQuestions()) {
			Collections.shuffle(views);
		}

		return new QuizDto(quiz.getId(), quiz.getCourseSectionId(), quiz.getTitle(), quiz.getDurationMinutes(),
				quiz.getMaxAttempts(), quiz.isShuffleQuestions(), quiz.isShowCorrectAnswers(), quiz.getOpenAt(),
				quiz.getCloseAt(), quiz.isPublished(), quiz.getCreatedAt(), views);
	}

	@Transactional
	public QuizAttemptDto startAttempt(UUID quizId, UUID studentId) {
		QuizEntity quiz = quizRepository.findById(quizId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz not found"));
		Instant now = Instant.now();
		if (!quiz.isPublished()) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Quiz is not published yet");
		}
		if (quiz.getOpenAt() != null && now.isBefore(quiz.getOpenAt())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Quiz has not opened yet");
		}
		if (quiz.getCloseAt() != null && now.isAfter(quiz.getCloseAt())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Quiz has already closed");
		}

		long count = quizAttemptRepository.countByQuizIdAndStudentId(quizId, studentId);
		if (count >= quiz.getMaxAttempts()) {
			throw new AppException(ErrorCode.CONFLICT, "Maximum quiz attempts reached");
		}

		QuizAttemptEntity attempt = new QuizAttemptEntity();
		attempt.setQuizId(quizId);
		attempt.setStudentId(studentId);
		attempt.setAttemptNumber((int) count + 1);
		attempt.setStatus("IN_PROGRESS");
		QuizAttemptEntity saved = quizAttemptRepository.save(attempt);
		return new QuizAttemptDto(saved.getId(), saved.getQuizId(), saved.getStudentId(), saved.getAttemptNumber(),
				saved.getStartedAt(), saved.getSubmittedAt(), saved.getScore(), saved.getStatus(), List.of());
	}

	@Transactional
	public QuizAttemptDto submitAttempt(UUID attemptId, UUID studentId, SubmitQuizRequest req) {
		QuizAttemptEntity attempt = quizAttemptRepository.findById(attemptId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz attempt not found"));
		if (!attempt.getStudentId().equals(studentId)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Cannot submit another student's attempt");
		}
		if (!"IN_PROGRESS".equals(attempt.getStatus())) {
			throw new AppException(ErrorCode.CONFLICT, "Quiz attempt already submitted");
		}

		QuizEntity quiz = quizRepository.findById(attempt.getQuizId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz not found"));
		List<QuizQuestionEntity> links = quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quiz.getId());
		Map<UUID, QuizQuestionEntity> linkByQId = links.stream()
				.collect(Collectors.toMap(QuizQuestionEntity::getQuestionId, Function.identity()));
		Map<UUID, QuestionEntity> questionMap = questionRepository
				.findAllById(links.stream().map(QuizQuestionEntity::getQuestionId).toList()).stream()
				.collect(Collectors.toMap(QuestionEntity::getId, Function.identity()));
		Map<UUID, List<String>> submittedMap = req.answers().stream()
				.collect(Collectors.toMap(AnswerInput::questionId, AnswerInput::selectedAnswers, (a, b) -> b));

		BigDecimal totalScore = BigDecimal.ZERO;
		for (QuizQuestionEntity link : links) {
			QuestionEntity q = questionMap.get(link.getQuestionId());
			List<String> selected = submittedMap.getOrDefault(link.getQuestionId(), List.of());
			Set<String> expectedSet = new HashSet<>(fromJsonList(q.getCorrectAnswersJson()));
			Set<String> selectedSet = new HashSet<>(selected);
			boolean isCorrect = !expectedSet.isEmpty() && expectedSet.equals(selectedSet);
			BigDecimal earned = isCorrect ? link.getPoints() : BigDecimal.ZERO;
			totalScore = totalScore.add(earned);

			QuizAnswerEntity ans = new QuizAnswerEntity();
			ans.setAttemptId(attempt.getId());
			ans.setQuestionId(link.getQuestionId());
			ans.setSelectedAnswersJson(toJson(selected));
			ans.setCorrect(isCorrect);
			ans.setEarnedPoints(earned);
			quizAnswerRepository.save(ans);
		}

		attempt.setSubmittedAt(Instant.now());
		attempt.setScore(totalScore);
		attempt.setStatus("GRADED");
		QuizAttemptEntity saved = quizAttemptRepository.save(attempt);
		return buildAttemptDto(saved, quiz.isShowCorrectAnswers(), questionMap, linkByQId);
	}

	@Transactional(readOnly = true)
	public List<QuizAttemptDto> listMyAttempts(UUID quizId, UUID studentId) {
		QuizEntity quiz = quizRepository.findById(quizId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Quiz not found"));
		List<QuizQuestionEntity> links = quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
		Map<UUID, QuestionEntity> questionMap = questionRepository
				.findAllById(links.stream().map(QuizQuestionEntity::getQuestionId).toList()).stream()
				.collect(Collectors.toMap(QuestionEntity::getId, Function.identity()));
		Map<UUID, QuizQuestionEntity> linkByQId = links.stream()
				.collect(Collectors.toMap(QuizQuestionEntity::getQuestionId, Function.identity()));

		return quizAttemptRepository.findByQuizIdAndStudentIdOrderByAttemptNumberDesc(quizId, studentId).stream()
				.map(a -> buildAttemptDto(a, quiz.isShowCorrectAnswers(), questionMap, linkByQId)).toList();
	}

	private QuizAttemptDto buildAttemptDto(QuizAttemptEntity attempt, boolean showCorrectAnswers,
			Map<UUID, QuestionEntity> questionMap, Map<UUID, QuizQuestionEntity> linkByQId) {
		List<QuizAnswerResultDto> results = quizAnswerRepository.findByAttemptId(attempt.getId()).stream().map(ans -> {
			QuestionEntity q = questionMap.get(ans.getQuestionId());
			List<String> correct = showCorrectAnswers && q != null
					? fromJsonList(q.getCorrectAnswersJson())
					: List.of();
			return new QuizAnswerResultDto(ans.getQuestionId(), fromJsonList(ans.getSelectedAnswersJson()), correct,
					ans.isCorrect(), ans.getEarnedPoints());
		}).toList();
		return new QuizAttemptDto(attempt.getId(), attempt.getQuizId(), attempt.getStudentId(),
				attempt.getAttemptNumber(), attempt.getStartedAt(), attempt.getSubmittedAt(), attempt.getScore(),
				attempt.getStatus(), results);
	}

	private QuestionDto toQuestionDto(QuestionEntity e) {
		List<String> tagList = e.getTags() == null || e.getTags().isBlank()
				? List.of()
				: List.of(e.getTags().split(","));
		return new QuestionDto(e.getId(), e.getSubjectId(), e.getCreatedBy(), e.getType(), e.getContent(),
				fromJsonList(e.getOptionsJson()), fromJsonList(e.getCorrectAnswersJson()), e.getDifficulty(),
				e.getTopic(), tagList, e.getCreatedAt());
	}

	private String toJson(List<String> list) {
		try {
			return objectMapper.writeValueAsString(list == null ? List.of() : list);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to serialize list");
		}
	}

	private List<String> fromJsonList(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(json, STRING_LIST_TYPE);
		} catch (JsonProcessingException ex) {
			return List.of();
		}
	}
}
