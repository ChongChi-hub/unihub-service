package com.unihub.ai.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentChunkRepository {

	private final JdbcTemplate jdbcTemplate;

	public DocumentChunkRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void saveChunk(UUID id, UUID courseSectionId, UUID lessonId, String fileId, int chunkIndex, String content,
			float[] embedding, String metadataJson) {
		String vectorLiteral = toVectorLiteral(embedding);
		jdbcTemplate.update(
				"INSERT INTO document_chunks (id, course_section_id, lesson_id, file_id, chunk_index, content, embedding, metadata_json) "
						+ "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS vector), ?)",
				id, courseSectionId, lessonId, fileId, chunkIndex, content, vectorLiteral, metadataJson);
	}

	public List<RetrievedChunk> searchSimilar(UUID courseSectionId, float[] queryEmbedding, int limit) {
		String vectorLiteral = toVectorLiteral(queryEmbedding);
		return jdbcTemplate.query(
				"SELECT id, lesson_id, file_id, chunk_index, content FROM document_chunks "
						+ "WHERE course_section_id = ? ORDER BY embedding <=> CAST(? AS vector) LIMIT ?",
				(rs, rowNum) -> new RetrievedChunk(UUID.fromString(rs.getString("id")),
						rs.getString("lesson_id") == null ? null : UUID.fromString(rs.getString("lesson_id")),
						rs.getString("file_id"), rs.getInt("chunk_index"), rs.getString("content")),
				courseSectionId, vectorLiteral, limit);
	}

	private static String toVectorLiteral(float[] embedding) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < embedding.length; i++) {
			if (i > 0)
				sb.append(',');
			sb.append(embedding[i]);
		}
		sb.append(']');
		return sb.toString();
	}

	public record RetrievedChunk(UUID id, UUID lessonId, String fileId, int chunkIndex, String content) {
	}
}
