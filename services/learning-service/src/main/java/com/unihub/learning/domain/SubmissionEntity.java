package com.unihub.learning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
public class SubmissionEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "assignment_id", nullable = false)
	private AssignmentEntity assignment;

	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Column(name = "file_id")
	private String fileId;

	@Column(name = "text_content", columnDefinition = "text")
	private String textContent;

	@Column(nullable = false)
	private int version = 1;

	@Column(name = "is_late", nullable = false)
	private boolean late = false;

	@Column(precision = 6, scale = 2)
	private BigDecimal score;

	@Column(columnDefinition = "text")
	private String feedback;

	@Column(name = "graded_by")
	private UUID gradedBy;

	@Column(name = "submitted_at", nullable = false)
	private Instant submittedAt;

	@Column(name = "graded_at")
	private Instant gradedAt;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (submittedAt == null) {
			submittedAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public AssignmentEntity getAssignment() {
		return assignment;
	}

	public void setAssignment(AssignmentEntity assignment) {
		this.assignment = assignment;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public void setStudentId(UUID studentId) {
		this.studentId = studentId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isLate() {
		return late;
	}

	public void setLate(boolean late) {
		this.late = late;
	}

	public BigDecimal getScore() {
		return score;
	}

	public void setScore(BigDecimal score) {
		this.score = score;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public UUID getGradedBy() {
		return gradedBy;
	}

	public void setGradedBy(UUID gradedBy) {
		this.gradedBy = gradedBy;
	}

	public Instant getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(Instant submittedAt) {
		this.submittedAt = submittedAt;
	}

	public Instant getGradedAt() {
		return gradedAt;
	}

	public void setGradedAt(Instant gradedAt) {
		this.gradedAt = gradedAt;
	}
}
