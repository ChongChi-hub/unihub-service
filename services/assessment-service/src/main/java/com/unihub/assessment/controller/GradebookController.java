package com.unihub.assessment.controller;

import com.unihub.assessment.dto.AssessmentDtos.GradeComponentDto;
import com.unihub.assessment.dto.AssessmentDtos.GradeComponentRequest;
import com.unihub.assessment.dto.AssessmentDtos.StudentGradeDto;
import com.unihub.assessment.dto.AssessmentDtos.StudentGradeRequest;
import com.unihub.assessment.service.GradebookService;
import com.unihub.common.api.ApiResponse;
import com.unihub.assessment.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/grades")
public class GradebookController {

	private final GradebookService gradebookService;

	public GradebookController(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}

	@PostMapping("/components")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<GradeComponentDto> createComponent(@Valid @RequestBody GradeComponentRequest request) {
		return ApiResponse.ok("Grade component created", gradebookService.createComponent(request));
	}

	@GetMapping("/section/{courseSectionId}/components")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<GradeComponentDto>> listComponents(@PathVariable UUID courseSectionId) {
		return ApiResponse.ok(gradebookService.listComponents(courseSectionId));
	}

	@PutMapping
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<StudentGradeDto> upsertGrade(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@Valid @RequestBody StudentGradeRequest request) {
		return ApiResponse.ok("Grade saved", gradebookService.upsertGrade(principal.userId(), request));
	}

	@PatchMapping("/components/{componentId}/lock")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<GradeComponentDto> lockComponent(@PathVariable UUID componentId) {
		return ApiResponse.ok("Grade component locked", gradebookService.lockComponent(componentId));
	}

	@GetMapping("/section/{courseSectionId}")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<List<StudentGradeDto>> listSectionGrades(@PathVariable UUID courseSectionId) {
		return ApiResponse.ok(gradebookService.listSectionGrades(courseSectionId));
	}

	@GetMapping("/section/{courseSectionId}/my")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<List<StudentGradeDto>> listMyGrades(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID courseSectionId) {
		return ApiResponse.ok(gradebookService.listMyGrades(courseSectionId, principal.userId()));
	}
}
