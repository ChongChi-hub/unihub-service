package com.unihub.assessment.controller;

import com.unihub.assessment.dto.AssessmentDtos.AttendanceSessionDto;
import com.unihub.assessment.dto.AssessmentDtos.AttendanceSessionRequest;
import com.unihub.assessment.service.AttendanceService;
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
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

	private final AttendanceService attendanceService;

	public AttendanceController(AttendanceService attendanceService) {
		this.attendanceService = attendanceService;
	}

	@PostMapping("/sessions")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<AttendanceSessionDto> createSession(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@Valid @RequestBody AttendanceSessionRequest request) {
		return ApiResponse.ok("Attendance recorded", attendanceService.createSession(principal.userId(), request));
	}

	@GetMapping("/section/{courseSectionId}")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<AttendanceSessionDto>> listBySection(@PathVariable UUID courseSectionId) {
		return ApiResponse.ok(attendanceService.listBySection(courseSectionId));
	}
}
