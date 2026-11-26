package com.unihub.learning.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.learning.dto.LearningDtos.AttachmentDto;
import com.unihub.learning.dto.LearningDtos.AttachmentRequest;
import com.unihub.learning.dto.LearningDtos.ChapterDto;
import com.unihub.learning.dto.LearningDtos.ChapterRequest;
import com.unihub.learning.dto.LearningDtos.LessonDto;
import com.unihub.learning.dto.LearningDtos.LessonRequest;
import com.unihub.learning.dto.LearningDtos.ProgressSummaryDto;
import com.unihub.learning.dto.LearningDtos.ReorderRequest;
import com.unihub.learning.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.learning.service.LearningContentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

	private final LearningContentService contentService;

	public LearningController(LearningContentService contentService) {
		this.contentService = contentService;
	}

	@GetMapping("/sections/{courseSectionId}/outline")
	public ApiResponse<List<ChapterDto>> outline(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID courseSectionId) {
		boolean includeDrafts = principal.roles().contains("ADMIN") || principal.roles().contains("LECTURER");
		return ApiResponse.ok(contentService.getSectionOutline(courseSectionId, includeDrafts));
	}

	@PostMapping("/chapters")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<ChapterDto> createChapter(@Valid @RequestBody ChapterRequest request) {
		return ApiResponse.ok("Chapter created", contentService.createChapter(request));
	}

	@PutMapping("/chapters/reorder")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<Void> reorderChapters(@Valid @RequestBody ReorderRequest request) {
		contentService.reorderChapters(request);
		return ApiResponse.okMessage("Chapters reordered");
	}

	@PostMapping("/lessons")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<LessonDto> createLesson(@Valid @RequestBody LessonRequest request) {
		return ApiResponse.ok("Lesson created", contentService.createLesson(request));
	}

	@PostMapping("/lessons/{lessonId}/attachments")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<AttachmentDto> addAttachment(@PathVariable UUID lessonId,
			@Valid @RequestBody AttachmentRequest request) {
		return ApiResponse.ok("Attachment added", contentService.addAttachment(lessonId, request));
	}

	@DeleteMapping("/attachments/{attachmentId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<Void> removeAttachment(@PathVariable UUID attachmentId) {
		contentService.removeAttachment(attachmentId);
		return ApiResponse.okMessage("Attachment removed");
	}

	@PostMapping("/lessons/{lessonId}/complete")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<ProgressSummaryDto> completeLesson(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID lessonId) {
		return ApiResponse.ok(contentService.markLessonCompleted(principal.userId(), lessonId));
	}

	@GetMapping("/sections/{courseSectionId}/progress")
	public ApiResponse<ProgressSummaryDto> getProgress(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID courseSectionId, @RequestParam(required = false) UUID studentId) {
		UUID targetStudent = (studentId != null
				&& (principal.roles().contains("ADMIN") || principal.roles().contains("LECTURER")))
						? studentId
						: principal.userId();
		return ApiResponse.ok(contentService.getSectionProgress(targetStudent, courseSectionId));
	}
}
