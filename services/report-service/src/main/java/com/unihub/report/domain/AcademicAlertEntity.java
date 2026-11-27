package com.unihub.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "academic_alerts")
public class AcademicAlertEntity {

	@Id
	private UUID id;

	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Column(name = "course_section_id", nullable = false)
	private UUID courseSectionId;

	@Column(name = "rule_id", nullable = false)
	private UUID ruleId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String reason;

	@Column(nullable = false)
	private boolean resolved;

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

	public UUID getStudentId() {
		return studentId;
	}

	public void setStudentId(UUID studentId) {
		this.studentId = studentId;
	}

	public UUID getCourseSectionId() {
		return courseSectionId;
	}

	public void setCourseSectionId(UUID courseSectionId) {
		this.courseSectionId = courseSectionId;
	}

	public UUID getRuleId() {
		return ruleId;
	}

	public void setRuleId(UUID ruleId) {
		this.ruleId = ruleId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
