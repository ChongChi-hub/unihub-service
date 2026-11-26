package com.unihub.notification.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.notification.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.notification.dto.NotificationDtos.AnnouncementDto;
import com.unihub.notification.dto.NotificationDtos.AnnouncementRequest;
import com.unihub.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

	private final NotificationService notificationService;

	public AnnouncementController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<AnnouncementDto> create(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@Valid @RequestBody AnnouncementRequest request) {
		return ApiResponse.ok("Announcement created",
				notificationService.createAnnouncement(principal.userId(), request));
	}

	@GetMapping("/section/{courseSectionId}")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<AnnouncementDto>> listBySection(@PathVariable UUID courseSectionId) {
		return ApiResponse.ok(notificationService.listAnnouncementsBySection(courseSectionId));
	}

	@DeleteMapping("/{announcementId}")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<Void> delete(@PathVariable UUID announcementId) {
		notificationService.deleteAnnouncement(announcementId);
		return ApiResponse.okMessage("Announcement deleted");
	}
}
