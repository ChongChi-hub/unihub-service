package com.unihub.notification.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.notification.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.notification.dto.NotificationDtos.CreateNotificationRequest;
import com.unihub.notification.dto.NotificationDtos.NotificationDto;
import com.unihub.notification.dto.NotificationDtos.UnreadCountDto;
import com.unihub.notification.service.NotificationService;
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
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<NotificationDto>> listMy(@AuthenticationPrincipal GatewayUserPrincipal principal) {
		return ApiResponse.ok(notificationService.listMyNotifications(principal.userId()));
	}

	@GetMapping("/unread-count")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<UnreadCountDto> unreadCount(@AuthenticationPrincipal GatewayUserPrincipal principal) {
		return ApiResponse.ok(notificationService.getUnreadCount(principal.userId()));
	}

	@PatchMapping("/{notificationId}/read")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<NotificationDto> markRead(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID notificationId) {
		return ApiResponse.ok("Notification marked as read",
				notificationService.markAsRead(notificationId, principal.userId()));
	}

	@PatchMapping("/read-all")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> markAllRead(@AuthenticationPrincipal GatewayUserPrincipal principal) {
		notificationService.markAllAsRead(principal.userId());
		return ApiResponse.okMessage("All notifications marked as read");
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
	public ApiResponse<NotificationDto> create(@Valid @RequestBody CreateNotificationRequest request) {
		return ApiResponse.ok("Notification created", notificationService.createNotification(request));
	}
}
