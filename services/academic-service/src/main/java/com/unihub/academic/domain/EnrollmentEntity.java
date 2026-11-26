package com.unihub.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
public class EnrollmentEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_section_id", nullable = false)
	private CourseSectionEntity courseSection;

	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

	@Column(name = "enrolled_at", nullable = false)
	private Instant enrolledAt;

	@Column(name = "dropped_at")
	private Instant droppedAt;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (enrolledAt == null) {
			enrolledAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public CourseSectionEntity getCourseSection() {
		return courseSection;
	}

	public void setCourseSection(CourseSectionEntity courseSection) {
		this.courseSection = courseSection;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public void setStudentId(UUID studentId) {
		this.studentId = studentId;
	}

	public EnrollmentStatus getStatus() {
		return status;
	}

	public void setStatus(EnrollmentStatus status) {
		this.status = status;
	}

	public Instant getEnrolledAt() {
		return enrolledAt;
	}

	public void setEnrolledAt(Instant enrolledAt) {
		this.enrolledAt = enrolledAt;
	}

	public Instant getDroppedAt() {
		return droppedAt;
	}

	public void setDroppedAt(Instant droppedAt) {
		this.droppedAt = droppedAt;
	}
}
