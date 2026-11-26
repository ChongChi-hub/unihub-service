package com.unihub.learning.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.learning.dto.LearningDtos.AssignmentDto;
import com.unihub.learning.dto.LearningDtos.AssignmentRequest;
import com.unihub.learning.dto.LearningDtos.GradeSubmissionRequest;
import com.unihub.learning.dto.LearningDtos.SubmissionDto;
import com.unihub.learning.dto.LearningDtos.SubmissionRequest;
import com.unihub.learning.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.learning.service.AssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

	private final AssignmentService assignmentService;

	public AssignmentController(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	@GetMapping("/section/{courseSectionId}")
	public ApiResponse<List<AssignmentDto>> listBySection(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID courseSectionId) {
		boolean includeDrafts = principal.roles().contains("ADMIN") || principal.roles().contains("LECTURER");
		return ApiResponse.ok(assignmentService.listBySection(courseSectionId, includeDrafts));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<AssignmentDto> create(@Valid @RequestBody AssignmentRequest request) {
		return ApiResponse.ok("Assignment created", assignmentService.create(request));
	}

	@PostMapping("/{assignmentId}/submit")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<SubmissionDto> submit(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID assignmentId, @Valid @RequestBody SubmissionRequest request) {
		return ApiResponse.ok("Assignment submitted",
				assignmentService.submit(principal.userId(), assignmentId, request));
	}

	@GetMapping("/{assignmentId}/submissions")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<List<SubmissionDto>> listSubmissions(@PathVariable UUID assignmentId) {
		return ApiResponse.ok(assignmentService.listSubmissionsForAssignment(assignmentId));
	}

	@GetMapping("/{assignmentId}/my-submissions")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<List<SubmissionDto>> mySubmissions(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID assignmentId) {
		return ApiResponse.ok(assignmentService.listStudentSubmissions(principal.userId(), assignmentId));
	}

	@PutMapping("/submissions/{submissionId}/grade")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<SubmissionDto> grade(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID submissionId, @Valid @RequestBody GradeSubmissionRequest request) {
		return ApiResponse.ok("Submission graded", assignmentService.grade(principal.userId(), submissionId, request));
	}
}
