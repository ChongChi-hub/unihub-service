package com.unihub.assessment.controller;

import com.unihub.assessment.dto.AssessmentDtos.QuizAttemptDto;
import com.unihub.assessment.dto.AssessmentDtos.QuizDto;
import com.unihub.assessment.dto.AssessmentDtos.QuizRequest;
import com.unihub.assessment.dto.AssessmentDtos.SubmitQuizRequest;
import com.unihub.assessment.service.QuizService;
import com.unihub.common.api.ApiResponse;
import com.unihub.assessment.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

	private final QuizService quizService;

	public QuizController(QuizService quizService) {
		this.quizService = quizService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<QuizDto> createQuiz(@Valid @RequestBody QuizRequest request) {
		return ApiResponse.ok("Quiz created", quizService.createQuiz(request));
	}

	@GetMapping("/section/{courseSectionId}")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<QuizDto>> listBySection(@PathVariable UUID courseSectionId) {
		return ApiResponse.ok(quizService.listQuizzesBySection(courseSectionId));
	}

	@GetMapping("/{quizId}")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<QuizDto> getQuiz(@PathVariable UUID quizId) {
		return ApiResponse.ok(quizService.getQuizDetail(quizId, true));
	}

	@PostMapping("/{quizId}/attempts")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<QuizAttemptDto> startAttempt(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID quizId) {
		return ApiResponse.ok("Quiz attempt started", quizService.startAttempt(quizId, principal.userId()));
	}

	@PostMapping("/attempts/{attemptId}/submit")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<QuizAttemptDto> submitAttempt(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID attemptId, @Valid @RequestBody SubmitQuizRequest request) {
		return ApiResponse.ok("Quiz submitted", quizService.submitAttempt(attemptId, principal.userId(), request));
	}

	@GetMapping("/{quizId}/my-attempts")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<List<QuizAttemptDto>> listMyAttempts(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID quizId) {
		return ApiResponse.ok(quizService.listMyAttempts(quizId, principal.userId()));
	}
}
