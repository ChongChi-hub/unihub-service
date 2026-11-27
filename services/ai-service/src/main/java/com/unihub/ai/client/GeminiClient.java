package com.unihub.ai.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

	private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
	public static final int EMBEDDING_DIM = 768;

	private final RestClient restClient;
	private final String apiKey;
	private final String chatModel;
	private final String embeddingModel;

	public GeminiClient(
			@Value("${app.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
			@Value("${app.gemini.api-key:}") String apiKey,
			@Value("${app.gemini.chat-model:gemini-2.0-flash}") String chatModel,
			@Value("${app.gemini.embedding-model:text-embedding-004}") String embeddingModel) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
		this.apiKey = apiKey;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
	}

	@SuppressWarnings("unchecked")
	public float[] embedText(String text) {
		if (apiKey == null || apiKey.isBlank()) {
			return fallbackEmbedding(text);
		}
		try {
			Map<String, Object> body = Map.of("model", "models/" + embeddingModel, "content",
					Map.of("parts", List.of(Map.of("text", text))));
			Map<String, Object> res = restClient.post()
					.uri("/models/{model}:embedContent?key={key}", embeddingModel, apiKey)
					.contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
			if (res != null && res.get("embedding") instanceof Map<?, ?> emb
					&& emb.get("values") instanceof List<?> values) {
				float[] result = new float[values.size()];
				for (int i = 0; i < values.size(); i++) {
					result[i] = ((Number) values.get(i)).floatValue();
				}
				return result;
			}
		} catch (Exception ex) {
			log.warn("Gemini embedding fallback triggered: {}", ex.getMessage());
		}
		return fallbackEmbedding(text);
	}

	@SuppressWarnings("unchecked")
	public String generateAnswer(String prompt, String contextSummary) {
		if (apiKey == null || apiKey.isBlank()) {
			return "Trả lời tham khảo từ tài liệu học phần:\n" + contextSummary;
		}
		try {
			Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
			Map<String, Object> res = restClient.post()
					.uri("/models/{model}:generateContent?key={key}", chatModel, apiKey)
					.contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
			if (res != null && res.get("candidates") instanceof List<?> candidates && !candidates.isEmpty()
					&& candidates.get(0) instanceof Map<?, ?> first && first.get("content") instanceof Map<?, ?> content
					&& content.get("parts") instanceof List<?> parts && !parts.isEmpty()
					&& parts.get(0) instanceof Map<?, ?> part0 && part0.get("text") instanceof String text) {
				return text;
			}
		} catch (Exception ex) {
			log.warn("Gemini generateContent fallback triggered: {}", ex.getMessage());
		}
		return "Trả lời tham khảo từ tài liệu học phần:\n" + contextSummary;
	}

	static float[] fallbackEmbedding(String text) {
		float[] vec = new float[EMBEDDING_DIM];
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
			for (int i = 0; i < EMBEDDING_DIM; i++) {
				int b = digest[i % digest.length] & 0xFF;
				vec[i] = (b / 255.0f) * 2.0f - 1.0f;
			}
		} catch (Exception ignored) {
			vec[0] = 1.0f;
		}
		return vec;
	}
}
