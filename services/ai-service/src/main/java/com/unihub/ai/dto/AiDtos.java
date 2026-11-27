package com.unihub.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AiDtos {

	private AiDtos() {
	}

	public record IndexDocumentRequest(@NotNull UUID courseSectionId, UUID lessonId, String fileId,
			@NotBlank String content, String title) {
	}

	public record IndexDocumentResponse(int chunksIndexed) {
	}

	public record ChatRequest(@NotNull UUID courseSectionId, UUID conversationId, @NotBlank String question) {
	}

	public record CitationDto(UUID chunkId, UUID lessonId, String fileId, int chunkIndex, String excerpt) {
	}

	public record ChatResponse(UUID conversationId, UUID messageId, String answer, List<CitationDto> citations) {
	}

	public record ConversationDto(UUID id, UUID courseSectionId, String title, Instant createdAt) {
	}

	public record MessageDto(UUID id, UUID conversationId, String role, String content, List<CitationDto> citations,
			Instant createdAt) {
	}
}
