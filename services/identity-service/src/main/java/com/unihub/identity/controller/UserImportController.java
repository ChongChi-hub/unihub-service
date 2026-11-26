package com.unihub.identity.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.identity.dto.UserImportPreviewResponse;
import com.unihub.identity.dto.UserImportResultResponse;
import com.unihub.identity.security.GatewayUserPrincipal;
import com.unihub.identity.service.UserImportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/import")
@PreAuthorize("hasRole('ADMIN')")
public class UserImportController {

	private final UserImportService userImportService;

	public UserImportController(UserImportService userImportService) {
		this.userImportService = userImportService;
	}

	@GetMapping("/template")
	public ResponseEntity<byte[]> downloadTemplate(@RequestParam(defaultValue = "xlsx") String format) {
		if ("csv".equalsIgnoreCase(format)) {
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users-import-template.csv\"")
					.contentType(MediaType.parseMediaType("text/csv")).body(userImportService.generateCsvTemplate());
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users-import-template.xlsx\"")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(userImportService.generateExcelTemplate());
	}

	@PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UserImportPreviewResponse> preview(@RequestParam("file") MultipartFile file) {
		return ApiResponse.ok(userImportService.preview(file));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UserImportResultResponse> importUsers(@AuthenticationPrincipal GatewayUserPrincipal actor,
			@RequestParam("file") MultipartFile file) {
		return ApiResponse.ok("Import processed", userImportService.importUsers(actor, file));
	}
}
