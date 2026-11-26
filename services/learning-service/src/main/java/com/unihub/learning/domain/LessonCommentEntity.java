package com.unihub.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson_comments")
public class LessonCommentEntity {

	@Id
	private UUID id;

	@Column(name = "lesson_id", nullable = false)
	private UUID lessonId;

	@Column(name = "parent_id")
	private UUID parentId;

	@Column(name = "author_id", nullable = false)
	private UUID authorId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "is_best_answer", nullable = false)
	private boolean bestAnswer;

	@Column(nullable = false)
	private boolean hidden;

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

	public UUID getLessonId() {
		return lessonId;
	}

	public void setLessonId(UUID lessonId) {
		this.lessonId = lessonId;
	}

	public UUID getParentId() {
		return parentId;
	}

	public void setParentId(UUID parentId) {
		this.parentId = parentId;
	}

	public UUID getAuthorId() {
		return authorId;
	}

	public void setAuthorId(UUID authorId) {
		this.authorId = authorId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isBestAnswer() {
		return bestAnswer;
	}

	public void setBestAnswer(boolean bestAnswer) {
		this.bestAnswer = bestAnswer;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
