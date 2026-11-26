package com.unihub.learning.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.learning.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.learning.service.LessonCommentService;
import com.unihub.learning.service.LessonCommentService.CreateCommentRequest;
import com.unihub.learning.service.LessonCommentService.LessonCommentDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning")
public class LessonCommentController {

	private final LessonCommentService commentService;

	public LessonCommentController(LessonCommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping("/lessons/{lessonId}/comments")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<LessonCommentDto>> listComments(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID lessonId) {
		boolean includeHidden = principal.roles().contains("ADMIN") || principal.roles().contains("LECTURER");
		return ApiResponse.ok(commentService.listComments(lessonId, includeHidden));
	}

	@PostMapping("/lessons/{lessonId}/comments")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<LessonCommentDto> createComment(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID lessonId, @Valid @RequestBody CommentPayload payload) {
		return ApiResponse.ok("Comment posted", commentService.createComment(lessonId, principal.userId(),
				new CreateCommentRequest(payload.parentId(), payload.content())));
	}

	@PatchMapping("/comments/{commentId}/best-answer")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<LessonCommentDto> toggleBestAnswer(@PathVariable UUID commentId) {
		return ApiResponse.ok("Best answer updated", commentService.toggleBestAnswer(commentId));
	}

	@PatchMapping("/comments/{commentId}/hide")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<LessonCommentDto> toggleHidden(@PathVariable UUID commentId) {
		return ApiResponse.ok("Visibility updated", commentService.toggleHidden(commentId));
	}

	public record CommentPayload(UUID parentId, @NotBlank String content) {
	}
}
