package com.unihub.identity.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.common.api.PageResponse;
import com.unihub.identity.domain.AuditLogEntity;
import com.unihub.identity.service.AuditLogService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

	private final AuditLogService auditLogService;

	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@GetMapping
	public ApiResponse<PageResponse<AuditLogEntity>> list(@RequestParam(required = false) String action,
			@RequestParam(required = false) String targetType, @RequestParam(required = false) String actorEmail,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ApiResponse.ok(auditLogService.search(action, targetType, actorEmail, pageable));
	}
}
