package com.unihub.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class QuestionEntity {

	@Id
	private UUID id;

	@Column(name = "subject_id", nullable = false)
	private UUID subjectId;

	@Column(name = "created_by", nullable = false)
	private UUID createdBy;

	@Column(nullable = false, length = 32)
	private String type;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "options_json", nullable = false, columnDefinition = "TEXT")
	private String optionsJson;

	@Column(name = "correct_answers_json", nullable = false, columnDefinition = "TEXT")
	private String correctAnswersJson;

	@Column(nullable = false, length = 16)
	private String difficulty = "MEDIUM";

	@Column(length = 128)
	private String topic;

	@Column(columnDefinition = "TEXT")
	private String tags;

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

	public UUID getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(UUID subjectId) {
		this.subjectId = subjectId;
	}

	public UUID getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UUID createdBy) {
		this.createdBy = createdBy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getOptionsJson() {
		return optionsJson;
	}

	public void setOptionsJson(String optionsJson) {
		this.optionsJson = optionsJson;
	}

	public String getCorrectAnswersJson() {
		return correctAnswersJson;
	}

	public void setCorrectAnswersJson(String correctAnswersJson) {
		this.correctAnswersJson = correctAnswersJson;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
