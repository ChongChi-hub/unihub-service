package com.unihub.ai.controller;

import com.unihub.ai.dto.AiDtos.ChatRequest;
import com.unihub.ai.dto.AiDtos.ChatResponse;
import com.unihub.ai.dto.AiDtos.ConversationDto;
import com.unihub.ai.dto.AiDtos.IndexDocumentRequest;
import com.unihub.ai.dto.AiDtos.IndexDocumentResponse;
import com.unihub.ai.dto.AiDtos.MessageDto;
import com.unihub.ai.security.GatewayHeaderAuthenticationFilter.GatewayUserPrincipal;
import com.unihub.ai.service.AiRagService;
import com.unihub.common.api.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

	private final AiRagService aiRagService;

	public AiController(AiRagService aiRagService) {
		this.aiRagService = aiRagService;
	}

	@PostMapping("/documents/index")
	@PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
	public ApiResponse<IndexDocumentResponse> indexDocument(@Valid @RequestBody IndexDocumentRequest request) {
		return ApiResponse.ok("Document indexed", aiRagService.indexDocument(request));
	}

	@PostMapping("/chat")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<ChatResponse> chat(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@Valid @RequestBody ChatRequest request) {
		return ApiResponse.ok(aiRagService.chat(principal.userId(), request));
	}

	@GetMapping("/conversations")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<ConversationDto>> listConversations(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@RequestParam UUID courseSectionId) {
		return ApiResponse.ok(aiRagService.listConversations(principal.userId(), courseSectionId));
	}

	@GetMapping("/conversations/{conversationId}/messages")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<MessageDto>> listMessages(@AuthenticationPrincipal GatewayUserPrincipal principal,
			@PathVariable UUID conversationId) {
		return ApiResponse.ok(aiRagService.listMessages(principal.userId(), conversationId));
	}
}
