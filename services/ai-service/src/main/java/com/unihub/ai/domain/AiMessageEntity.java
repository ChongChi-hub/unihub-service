package com.unihub.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_messages")
public class AiMessageEntity {

	@Id
	private UUID id;

	@Column(name = "conversation_id", nullable = false)
	private UUID conversationId;

	@Column(nullable = false, length = 16)
	private String role;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "citations_json", columnDefinition = "TEXT")
	private String citationsJson;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (id == null)
			id = UUID.randomUUID();
		if (createdAt == null)
			createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getConversationId() {
		return conversationId;
	}

	public void setConversationId(UUID conversationId) {
		this.conversationId = conversationId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCitationsJson() {
		return citationsJson;
	}

	public void setCitationsJson(String citationsJson) {
		this.citationsJson = citationsJson;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
