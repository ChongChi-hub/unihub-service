package com.unihub.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.ai.client.GeminiClient;
import com.unihub.ai.domain.AiConversationEntity;
import com.unihub.ai.domain.AiMessageEntity;
import com.unihub.ai.dto.AiDtos.ChatRequest;
import com.unihub.ai.dto.AiDtos.ChatResponse;
import com.unihub.ai.dto.AiDtos.CitationDto;
import com.unihub.ai.dto.AiDtos.ConversationDto;
import com.unihub.ai.dto.AiDtos.IndexDocumentRequest;
import com.unihub.ai.dto.AiDtos.IndexDocumentResponse;
import com.unihub.ai.dto.AiDtos.MessageDto;
import com.unihub.ai.repository.AiConversationRepository;
import com.unihub.ai.repository.AiMessageRepository;
import com.unihub.ai.repository.DocumentChunkRepository;
import com.unihub.ai.repository.DocumentChunkRepository.RetrievedChunk;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiRagService {

	private static final int CHUNK_SIZE = 800;
	private static final int TOP_K = 4;
	private static final TypeReference<List<CitationDto>> CITATIONS_TYPE = new TypeReference<>() {
	};

	private final DocumentChunkRepository chunkRepository;
	private final AiConversationRepository conversationRepository;
	private final AiMessageRepository messageRepository;
	private final GeminiClient geminiClient;
	private final ObjectMapper objectMapper;

	public AiRagService(DocumentChunkRepository chunkRepository, AiConversationRepository conversationRepository,
			AiMessageRepository messageRepository, GeminiClient geminiClient, ObjectMapper objectMapper) {
		this.chunkRepository = chunkRepository;
		this.conversationRepository = conversationRepository;
		this.messageRepository = messageRepository;
		this.geminiClient = geminiClient;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public IndexDocumentResponse indexDocument(IndexDocumentRequest req) {
		List<String> chunks = splitIntoChunks(req.content().trim(), CHUNK_SIZE);
		String metadata = req.title() == null ? null : "{\"title\":\"" + req.title().replace("\"", "'") + "\"}";
		for (int i = 0; i < chunks.size(); i++) {
			String chunkText = chunks.get(i);
			float[] embedding = geminiClient.embedText(chunkText);
			chunkRepository.saveChunk(UUID.randomUUID(), req.courseSectionId(), req.lessonId(), req.fileId(), i,
					chunkText, embedding, metadata);
		}
		return new IndexDocumentResponse(chunks.size());
	}

	@Transactional
	public ChatResponse chat(UUID userId, ChatRequest req) {
		AiConversationEntity conversation;
		if (req.conversationId() != null) {
			conversation = conversationRepository.findById(req.conversationId())
					.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Conversation not found"));
			if (!conversation.getUserId().equals(userId)) {
				throw new AppException(ErrorCode.FORBIDDEN, "Cannot access another user's conversation");
			}
		} else {
			conversation = new AiConversationEntity();
			conversation.setUserId(userId);
			conversation.setCourseSectionId(req.courseSectionId());
			String q = req.question().trim();
			conversation.setTitle(q.length() > 80 ? q.substring(0, 80) + "..." : q);
			conversation = conversationRepository.save(conversation);
		}

		AiMessageEntity userMsg = new AiMessageEntity();
		userMsg.setConversationId(conversation.getId());
		userMsg.setRole("USER");
		userMsg.setContent(req.question().trim());
		messageRepository.save(userMsg);

		float[] queryEmbedding = geminiClient.embedText(req.question().trim());
		List<RetrievedChunk> retrieved = chunkRepository.searchSimilar(req.courseSectionId(), queryEmbedding, TOP_K);

		String context = retrieved.isEmpty()
				? "Chưa có tài liệu nào được lập chỉ mục cho lớp học phần này."
				: retrieved.stream().map(c -> "[Đoạn " + c.chunkIndex() + "] " + c.content())
						.collect(Collectors.joining("\n\n"));

		String prompt = "Bạn là trợ giảng AI của Unihub. Chỉ trả lời dựa trên ngữ cảnh tài liệu sau:\n\n" + context
				+ "\n\nCâu hỏi của sinh viên: " + req.question().trim();
		String answer = geminiClient.generateAnswer(prompt, context);

		List<CitationDto> citations = retrieved.stream().map(c -> new CitationDto(c.id(), c.lessonId(), c.fileId(),
				c.chunkIndex(), c.content().length() > 160 ? c.content().substring(0, 160) + "..." : c.content()))
				.toList();

		AiMessageEntity assistantMsg = new AiMessageEntity();
		assistantMsg.setConversationId(conversation.getId());
		assistantMsg.setRole("ASSISTANT");
		assistantMsg.setContent(answer);
		assistantMsg.setCitationsJson(toJson(citations));
		AiMessageEntity savedAssistant = messageRepository.save(assistantMsg);

		return new ChatResponse(conversation.getId(), savedAssistant.getId(), answer, citations);
	}

	@Transactional(readOnly = true)
	public List<ConversationDto> listConversations(UUID userId, UUID courseSectionId) {
		return conversationRepository.findByUserIdAndCourseSectionIdOrderByCreatedAtDesc(userId, courseSectionId)
				.stream()
				.map(c -> new ConversationDto(c.getId(), c.getCourseSectionId(), c.getTitle(), c.getCreatedAt()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MessageDto> listMessages(UUID userId, UUID conversationId) {
		AiConversationEntity conv = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Conversation not found"));
		if (!conv.getUserId().equals(userId)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Cannot access another user's conversation");
		}
		return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
				.map(m -> new MessageDto(m.getId(), m.getConversationId(), m.getRole(), m.getContent(),
						fromJson(m.getCitationsJson()), m.getCreatedAt()))
				.toList();
	}

	static List<String> splitIntoChunks(String text, int chunkSize) {
		List<String> list = new ArrayList<>();
		if (text == null || text.isEmpty())
			return list;
		for (int i = 0; i < text.length(); i += chunkSize) {
			list.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
		}
		return list;
	}

	private String toJson(List<CitationDto> citations) {
		try {
			return objectMapper.writeValueAsString(citations);
		} catch (JsonProcessingException ex) {
			return "[]";
		}
	}

	private List<CitationDto> fromJson(String json) {
		if (json == null || json.isBlank())
			return List.of();
		try {
			return objectMapper.readValue(json, CITATIONS_TYPE);
		} catch (JsonProcessingException ex) {
			return List.of();
		}
	}
}
