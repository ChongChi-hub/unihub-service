package com.unihub.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {

	@Id
	private UUID id;

	@Column(name = "course_section_id", nullable = false)
	private UUID courseSectionId;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "attachment_file_id")
	private String attachmentFileId;

	@Column(name = "due_at", nullable = false)
	private Instant dueAt;

	@Column(name = "max_score", nullable = false, precision = 6, scale = 2)
	private BigDecimal maxScore = BigDecimal.TEN;

	@Column(name = "allow_late", nullable = false)
	private boolean allowLate = false;

	@Column(nullable = false)
	private boolean published = false;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getCourseSectionId() {
		return courseSectionId;
	}

	public void setCourseSectionId(UUID courseSectionId) {
		this.courseSectionId = courseSectionId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAttachmentFileId() {
		return attachmentFileId;
	}

	public void setAttachmentFileId(String attachmentFileId) {
		this.attachmentFileId = attachmentFileId;
	}

	public Instant getDueAt() {
		return dueAt;
	}

	public void setDueAt(Instant dueAt) {
		this.dueAt = dueAt;
	}

	public BigDecimal getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(BigDecimal maxScore) {
		this.maxScore = maxScore;
	}

	public boolean isAllowLate() {
		return allowLate;
	}

	public void setAllowLate(boolean allowLate) {
		this.allowLate = allowLate;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
