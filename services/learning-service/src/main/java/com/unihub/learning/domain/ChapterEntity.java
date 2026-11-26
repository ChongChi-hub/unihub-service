package com.unihub.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chapters")
public class ChapterEntity {

	@Id
	private UUID id;

	@Column(name = "course_section_id", nullable = false)
	private UUID courseSectionId;

	@Column(nullable = false)
	private String title;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

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

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
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
