package com.unihub.learning.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class LearningDtos {

	private LearningDtos() {
	}

	public record ChapterRequest(@NotNull UUID courseSectionId, @NotBlank String title, int sortOrder,
			boolean published) {
	}

	public record ChapterDto(UUID id, UUID courseSectionId, String title, int sortOrder, boolean published,
			List<LessonDto> lessons) {
	}

	public record LessonRequest(@NotNull UUID chapterId, @NotBlank String title, String content, String videoUrl,
			String videoFileId, int sortOrder, boolean published) {
	}

	public record AttachmentRequest(@NotBlank String fileId, @NotBlank String fileName, String contentType,
			long fileSize) {
	}

	public record AttachmentDto(UUID id, String fileId, String fileName, String contentType, long fileSize) {
	}

	public record LessonDto(UUID id, UUID chapterId, String title, String content, String videoUrl, String videoFileId,
			int sortOrder, boolean published, List<AttachmentDto> attachments) {
	}

	public record ReorderRequest(@NotEmpty List<UUID> orderedIds) {
	}

	public record ProgressSummaryDto(UUID courseSectionId, int totalLessons, int completedLessons,
			int progressPercent) {
	}

	public record AssignmentRequest(@NotNull UUID courseSectionId, @NotBlank String title, String description,
			String attachmentFileId, @NotNull Instant dueAt, @NotNull @DecimalMin("0.5") BigDecimal maxScore,
			boolean allowLate, boolean published) {
	}

	public record AssignmentDto(UUID id, UUID courseSectionId, String title, String description,
			String attachmentFileId, Instant dueAt, BigDecimal maxScore, boolean allowLate, boolean published,
			Instant createdAt) {
	}

	public record SubmissionRequest(String fileId, String textContent) {
	}

	public record GradeSubmissionRequest(@NotNull @DecimalMin("0.0") BigDecimal score, String feedback) {
	}

	public record SubmissionDto(UUID id, UUID assignmentId, UUID studentId, String fileId, String textContent,
			int version, boolean late, BigDecimal score, String feedback, Instant submittedAt, Instant gradedAt) {
	}
}
