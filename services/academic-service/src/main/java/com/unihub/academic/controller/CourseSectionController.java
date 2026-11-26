package com.unihub.academic.controller;

import com.unihub.academic.dto.CourseSectionDtos.AssignLecturersRequest;
import com.unihub.academic.dto.CourseSectionDtos.CourseSectionDto;
import com.unihub.academic.dto.CourseSectionDtos.CourseSectionRequest;
import com.unihub.academic.dto.CourseSectionDtos.EnrollmentDto;
import com.unihub.academic.dto.CourseSectionDtos.ManualEnrollmentRequest;
import com.unihub.academic.dto.CourseSectionDtos.RegistrationWindowRequest;
import com.unihub.academic.dto.CourseSectionDtos.TimetableSlotDto;
import com.unihub.academic.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.academic.service.CourseSectionService;
import com.unihub.academic.service.EnrollmentService;
import com.unihub.common.api.ApiResponse;
import com.unihub.common.api.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academic")
public class CourseSectionController {

	private final CourseSectionService sectionService;
	private final EnrollmentService enrollmentService;

	public CourseSectionController(CourseSectionService sectionService, EnrollmentService enrollmentService) {
		this.sectionService = sectionService;
		this.enrollmentService = enrollmentService;
	}

	@GetMapping("/sections")
	public ApiResponse<PageResponse<CourseSectionDto>> listSections(@RequestParam(required = false) UUID semesterId,
			@RequestParam(required = false) UUID subjectId, @RequestParam(required = false) UUID lecturerId,
			@PageableDefault(size = 20) Pageable pageable) {
		return ApiResponse.ok(sectionService.list(semesterId, subjectId, lecturerId, pageable));
	}

	@PostMapping("/sections")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<CourseSectionDto> createSection(@Valid @RequestBody CourseSectionRequest request) {
		return ApiResponse.ok("Course section opened", sectionService.create(request));
	}

	@PutMapping("/sections/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<CourseSectionDto> updateSection(@PathVariable UUID id,
			@Valid @RequestBody CourseSectionRequest request) {
		return ApiResponse.ok("Course section updated", sectionService.update(id, request));
	}

	@PatchMapping("/sections/{id}/close")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<CourseSectionDto> closeSection(@PathVariable UUID id) {
		return ApiResponse.ok("Course section closed", sectionService.close(id));
	}

	@PutMapping("/sections/{id}/lecturers")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<CourseSectionDto> assignLecturers(@PathVariable UUID id,
			@Valid @RequestBody AssignLecturersRequest request) {
		return ApiResponse.ok("Lecturers assigned", sectionService.assignLecturers(id, request));
	}

	@PutMapping("/semesters/{semesterId}/registration-window")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> configureRegistrationWindow(@PathVariable UUID semesterId,
			@Valid @RequestBody RegistrationWindowRequest request) {
		sectionService.configureRegistrationWindow(semesterId, request);
		return ApiResponse.okMessage("Registration window updated");
	}

	@GetMapping("/sections/{id}/students")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<List<EnrollmentDto>> listSectionStudents(@PathVariable UUID id) {
		return ApiResponse.ok(sectionService.listSectionStudents(id));
	}

	@PostMapping("/sections/{id}/students")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<EnrollmentDto> adminAddStudent(@PathVariable UUID id,
			@Valid @RequestBody ManualEnrollmentRequest request) {
		return ApiResponse.ok("Student added to section", sectionService.adminAddStudent(id, request));
	}

	@DeleteMapping("/sections/{id}/students/{studentId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> adminRemoveStudent(@PathVariable UUID id, @PathVariable UUID studentId) {
		sectionService.adminRemoveStudent(id, studentId);
		return ApiResponse.okMessage("Student removed from section");
	}

	@GetMapping("/sections/{id}/students/export")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ResponseEntity<byte[]> exportRoster(@PathVariable UUID id) {
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"section-" + id + "-roster.xlsx\"")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(sectionService.exportSectionRosterExcel(id));
	}

	@PostMapping("/sections/{id}/enroll")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<EnrollmentDto> enroll(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID id) {
		return ApiResponse.ok("Enrolled successfully", enrollmentService.enroll(principal.userId(), id));
	}

	@DeleteMapping("/sections/{id}/enroll")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<Void> drop(@AuthenticationPrincipal GatewayUserPrincipal principal, @PathVariable UUID id) {
		enrollmentService.drop(principal.userId(), id);
		return ApiResponse.okMessage("Dropped successfully");
	}

	@GetMapping("/timetable")
	@PreAuthorize("hasAnyRole('STUDENT', 'LECTURER')")
	public ApiResponse<List<TimetableSlotDto>> timetable(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@RequestParam UUID semesterId) {
		boolean isLecturer = principal.roles().contains("LECTURER");
		return ApiResponse.ok(enrollmentService.getTimetable(principal.userId(), semesterId, isLecturer));
	}
}
