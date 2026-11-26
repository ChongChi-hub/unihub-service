package com.unihub.assessment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AssessmentDtos {

	private AssessmentDtos() {
	}

	public record QuestionRequest(@NotNull UUID subjectId, @NotBlank String type, @NotBlank String content,
			@NotEmpty List<String> options, @NotEmpty List<String> correctAnswers, String difficulty, String topic,
			List<String> tags) {
	}

	public record QuestionDto(UUID id, UUID subjectId, UUID createdBy, String type, String content,
			List<String> options, List<String> correctAnswers, String difficulty, String topic, List<String> tags,
			Instant createdAt) {
	}

	public record QuizQuestionInput(@NotNull UUID questionId, int sortOrder,
			@NotNull @DecimalMin("0.25") BigDecimal points) {
	}

	public record QuizRequest(@NotNull UUID courseSectionId, @NotBlank String title, @Min(1) int durationMinutes,
			@Min(1) int maxAttempts, boolean shuffleQuestions, boolean showCorrectAnswers, Instant openAt,
			Instant closeAt, boolean published, @Valid List<QuizQuestionInput> questions) {
	}

	public record QuizQuestionView(UUID questionId, String type, String content, List<String> options, int sortOrder,
			BigDecimal points) {
	}

	public record QuizDto(UUID id, UUID courseSectionId, String title, int durationMinutes, int maxAttempts,
			boolean shuffleQuestions, boolean showCorrectAnswers, Instant openAt, Instant closeAt, boolean published,
			Instant createdAt, List<QuizQuestionView> questions) {
	}

	public record AnswerInput(@NotNull UUID questionId, @NotNull List<String> selectedAnswers) {
	}

	public record SubmitQuizRequest(@Valid @NotNull List<AnswerInput> answers) {
	}

	public record QuizAnswerResultDto(UUID questionId, List<String> selectedAnswers, List<String> correctAnswers,
			boolean correct, BigDecimal earnedPoints) {
	}

	public record QuizAttemptDto(UUID id, UUID quizId, UUID studentId, int attemptNumber, Instant startedAt,
			Instant submittedAt, BigDecimal score, String status, List<QuizAnswerResultDto> answers) {
	}

	public record GradeComponentRequest(@NotNull UUID courseSectionId, @NotBlank String name,
			@NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal weightPercent, String linkedType,
			UUID linkedId) {
	}

	public record GradeComponentDto(UUID id, UUID courseSectionId, String name, BigDecimal weightPercent,
			String linkedType, UUID linkedId, boolean locked) {
	}

	public record StudentGradeRequest(@NotNull UUID courseSectionId, @NotNull UUID studentId, @NotNull UUID componentId,
			@NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal score) {
	}

	public record StudentGradeDto(UUID id, UUID courseSectionId, UUID studentId, UUID componentId, BigDecimal score,
			boolean locked, UUID updatedBy, Instant updatedAt) {
	}

	public record AttendanceRecordInput(@NotNull UUID studentId, @NotBlank String status, String note) {
	}

	public record AttendanceSessionRequest(@NotNull UUID courseSectionId, @NotNull LocalDate sessionDate, String topic,
			@Valid List<AttendanceRecordInput> records) {
	}

	public record AttendanceRecordDto(UUID sessionId, UUID studentId, String status, String note) {
	}

	public record AttendanceSessionDto(UUID id, UUID courseSectionId, LocalDate sessionDate, String topic,
			UUID createdBy, Instant createdAt, List<AttendanceRecordDto> records) {
	}
}
