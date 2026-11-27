package com.unihub.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_academic_summaries")
public class StudentAcademicSummaryEntity {

	@Id
	private UUID id;

	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Column(name = "semester_id", nullable = false)
	private UUID semesterId;

	@Column(name = "course_section_id", nullable = false)
	private UUID courseSectionId;

	@Column(name = "attendance_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal attendanceRate = new BigDecimal("100.00");

	@Column(name = "current_score", nullable = false, precision = 5, scale = 2)
	private BigDecimal currentScore = BigDecimal.ZERO;

	@Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal progressPercent = BigDecimal.ZERO;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersist() {
		if (id == null)
			id = UUID.randomUUID();
		if (updatedAt == null)
			updatedAt = Instant.now();
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public void setStudentId(UUID studentId) {
		this.studentId = studentId;
	}

	public UUID getSemesterId() {
		return semesterId;
	}

	public void setSemesterId(UUID semesterId) {
		this.semesterId = semesterId;
	}

	public UUID getCourseSectionId() {
		return courseSectionId;
	}

	public void setCourseSectionId(UUID courseSectionId) {
		this.courseSectionId = courseSectionId;
	}

	public BigDecimal getAttendanceRate() {
		return attendanceRate;
	}

	public void setAttendanceRate(BigDecimal attendanceRate) {
		this.attendanceRate = attendanceRate;
	}

	public BigDecimal getCurrentScore() {
		return currentScore;
	}

	public void setCurrentScore(BigDecimal currentScore) {
		this.currentScore = currentScore;
	}

	public BigDecimal getProgressPercent() {
		return progressPercent;
	}

	public void setProgressPercent(BigDecimal progressPercent) {
		this.progressPercent = progressPercent;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
