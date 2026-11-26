package com.unihub.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson_progress")
@IdClass(LessonProgressEntity.ProgressKey.class)
public class LessonProgressEntity {

	@Id
	@Column(name = "lesson_id", nullable = false)
	private UUID lessonId;

	@Id
	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Column(name = "completed_at", nullable = false)
	private Instant completedAt;

	@PrePersist
	void onCreate() {
		if (completedAt == null) {
			completedAt = Instant.now();
		}
	}

	public UUID getLessonId() {
		return lessonId;
	}

	public void setLessonId(UUID lessonId) {
		this.lessonId = lessonId;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public void setStudentId(UUID studentId) {
		this.studentId = studentId;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public record ProgressKey(UUID lessonId, UUID studentId) implements Serializable {
	}
}
