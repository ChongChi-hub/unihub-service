package com.unihub.report.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.report.dto.ReportDtos.AcademicAlertDto;
import com.unihub.report.dto.ReportDtos.AlertRuleDto;
import com.unihub.report.dto.ReportDtos.AlertRuleRequest;
import com.unihub.report.dto.ReportDtos.SummaryDto;
import com.unihub.report.dto.ReportDtos.SummaryUpsertRequest;
import com.unihub.report.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.report.service.ReportService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/reports")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@PostMapping("/summaries")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<SummaryDto> upsertSummary(@Valid @RequestBody SummaryUpsertRequest request) {
		return ApiResponse.ok("Summary updated", reportService.upsertSummary(request));
	}

	@GetMapping("/students/my")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<List<SummaryDto>> mySummaries(@AuthenticationPrincipal GatewayUserPrincipal principal) {
		return ApiResponse.ok(reportService.listByStudent(principal.userId()));
	}

	@GetMapping("/sections/{courseSectionId}")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<List<SummaryDto>> sectionSummaries(@PathVariable UUID courseSectionId) {
		return ApiResponse.ok(reportService.listBySection(courseSectionId));
	}

	@PostMapping("/alerts/rules")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AlertRuleDto> createRule(@Valid @RequestBody AlertRuleRequest request) {
		return ApiResponse.ok("Alert rule created", reportService.createRule(request));
	}

	@GetMapping("/alerts/rules")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<List<AlertRuleDto>> listRules() {
		return ApiResponse.ok(reportService.listRules());
	}

	@GetMapping("/alerts")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<List<AcademicAlertDto>> listAlerts() {
		return ApiResponse.ok(reportService.listAlerts());
	}

	@GetMapping("/alerts/my")
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<List<AcademicAlertDto>> listMyAlerts(@AuthenticationPrincipal GatewayUserPrincipal principal) {
		return ApiResponse.ok(reportService.listMyAlerts(principal.userId()));
	}

	@PatchMapping("/alerts/{alertId}/resolve")
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<AcademicAlertDto> resolveAlert(@PathVariable UUID alertId) {
		return ApiResponse.ok("Alert resolved", reportService.resolveAlert(alertId));
	}
}
