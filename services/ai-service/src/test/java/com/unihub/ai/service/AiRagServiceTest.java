package com.unihub.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.ai.client.GeminiClient;
import com.unihub.ai.domain.AiConversationEntity;
import com.unihub.ai.domain.AiMessageEntity;
import com.unihub.ai.dto.AiDtos.ChatRequest;
import com.unihub.ai.dto.AiDtos.ChatResponse;
import com.unihub.ai.dto.AiDtos.IndexDocumentRequest;
import com.unihub.ai.dto.AiDtos.IndexDocumentResponse;
import com.unihub.ai.repository.AiConversationRepository;
import com.unihub.ai.repository.AiMessageRepository;
import com.unihub.ai.repository.DocumentChunkRepository;
import com.unihub.ai.repository.DocumentChunkRepository.RetrievedChunk;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiRagServiceTest {

	@Mock
	private DocumentChunkRepository chunkRepository;
	@Mock
	private AiConversationRepository conversationRepository;
	@Mock
	private AiMessageRepository messageRepository;
	@Mock
	private GeminiClient geminiClient;

	private AiRagService aiRagService;

	@BeforeEach
	void setUp() {
		aiRagService = new AiRagService(chunkRepository, conversationRepository, messageRepository, geminiClient,
				new ObjectMapper());
	}

	@Test
	void indexDocumentSplitsAndStoresChunks() {
		UUID sectionId = UUID.randomUUID();
		String text = "A".repeat(1000);
		when(geminiClient.embedText(any())).thenReturn(new float[768]);

		IndexDocumentResponse res = aiRagService
				.indexDocument(new IndexDocumentRequest(sectionId, null, null, text, "Lecture 1"));

		assertEquals(2, res.chunksIndexed());
		verify(chunkRepository, times(2)).saveChunk(any(), eq(sectionId), any(), any(), anyInt(), any(), any(), any());
	}

	@Test
	void chatRetrievesCitationsAndSavesMessages() {
		UUID userId = UUID.randomUUID();
		UUID sectionId = UUID.randomUUID();
		UUID convId = UUID.randomUUID();

		when(conversationRepository.save(any())).thenAnswer(inv -> {
			AiConversationEntity c = inv.getArgument(0);
			c.setId(convId);
			return c;
		});
		when(messageRepository.save(any())).thenAnswer(inv -> {
			AiMessageEntity m = inv.getArgument(0);
			if (m.getId() == null)
				m.setId(UUID.randomUUID());
			return m;
		});
		when(geminiClient.embedText(any())).thenReturn(new float[768]);
		when(chunkRepository.searchSimilar(eq(sectionId), any(), anyInt())).thenReturn(
				List.of(new RetrievedChunk(UUID.randomUUID(), null, null, 0, "Microservices communicate via HTTP")));
		when(geminiClient.generateAnswer(any(), any())).thenReturn("They communicate via HTTP.");

		ChatResponse res = aiRagService.chat(userId, new ChatRequest(sectionId, null, "How do services talk?"));

		assertEquals(convId, res.conversationId());
		assertEquals("They communicate via HTTP.", res.answer());
		assertFalse(res.citations().isEmpty());
	}
}
