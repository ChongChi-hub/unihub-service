package com.unihub.assessment.controller;

import com.unihub.assessment.dto.AssessmentDtos.QuestionDto;
import com.unihub.assessment.dto.AssessmentDtos.QuestionRequest;
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
@RequestMapping("/api/v1/questions")
public class QuestionController {

	private final QuizService quizService;

	public QuestionController(QuizService quizService) {
		this.quizService = quizService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<QuestionDto> createQuestion(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@Valid @RequestBody QuestionRequest request) {
		return ApiResponse.ok("Question created", quizService.createQuestion(principal.userId(), request));
	}

	@GetMapping("/subject/{subjectId}")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<List<QuestionDto>> listBySubject(@PathVariable UUID subjectId) {
		return ApiResponse.ok(quizService.listQuestionsBySubject(subjectId));
	}
}
