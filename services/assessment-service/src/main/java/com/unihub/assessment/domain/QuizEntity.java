package com.unihub.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
public class QuizEntity {

	@Id
	private UUID id;

	@Column(name = "course_section_id", nullable = false)
	private UUID courseSectionId;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(name = "duration_minutes", nullable = false)
	private int durationMinutes = 30;

	@Column(name = "max_attempts", nullable = false)
	private int maxAttempts = 1;

	@Column(name = "shuffle_questions", nullable = false)
	private boolean shuffleQuestions;

	@Column(name = "show_correct_answers", nullable = false)
	private boolean showCorrectAnswers = true;

	@Column(name = "open_at")
	private Instant openAt;

	@Column(name = "close_at")
	private Instant closeAt;

	@Column(nullable = false)
	private boolean published;

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

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public boolean isShuffleQuestions() {
		return shuffleQuestions;
	}

	public void setShuffleQuestions(boolean shuffleQuestions) {
		this.shuffleQuestions = shuffleQuestions;
	}

	public boolean isShowCorrectAnswers() {
		return showCorrectAnswers;
	}

	public void setShowCorrectAnswers(boolean showCorrectAnswers) {
		this.showCorrectAnswers = showCorrectAnswers;
	}

	public Instant getOpenAt() {
		return openAt;
	}

	public void setOpenAt(Instant openAt) {
		this.openAt = openAt;
	}

	public Instant getCloseAt() {
		return closeAt;
	}

	public void setCloseAt(Instant closeAt) {
		this.closeAt = closeAt;
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
