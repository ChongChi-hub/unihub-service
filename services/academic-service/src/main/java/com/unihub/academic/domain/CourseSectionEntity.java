package com.unihub.academic.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "course_sections")
public class CourseSectionEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "semester_id", nullable = false)
	private SemesterEntity semester;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subject_id", nullable = false)
	private SubjectEntity subject;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(name = "max_capacity", nullable = false)
	private int maxCapacity;

	@Column(name = "enrolled_count", nullable = false)
	private int enrolledCount = 0;

	@Column(length = 64)
	private String room;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CourseSectionStatus status = CourseSectionStatus.OPEN;

	@Version
	private long version;

	@OneToMany(mappedBy = "courseSection", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CourseSectionScheduleEntity> schedules = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "course_section_lecturers", joinColumns = @JoinColumn(name = "course_section_id"))
	@Column(name = "lecturer_id", nullable = false)
	private Set<UUID> lecturerIds = new HashSet<>();

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

	public SemesterEntity getSemester() {
		return semester;
	}

	public void setSemester(SemesterEntity semester) {
		this.semester = semester;
	}

	public SubjectEntity getSubject() {
		return subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public int getEnrolledCount() {
		return enrolledCount;
	}

	public void setEnrolledCount(int enrolledCount) {
		this.enrolledCount = enrolledCount;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public CourseSectionStatus getStatus() {
		return status;
	}

	public void setStatus(CourseSectionStatus status) {
		this.status = status;
	}

	public List<CourseSectionScheduleEntity> getSchedules() {
		return schedules;
	}

	public Set<UUID> getLecturerIds() {
		return lecturerIds;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
