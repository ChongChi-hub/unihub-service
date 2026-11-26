package com.unihub.academic.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "admin_classes")
public class AdminClassEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "major_id", nullable = false)
	private MajorEntity major;

	@Column(nullable = false, unique = true, length = 32)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(name = "cohort_year", nullable = false)
	private int cohortYear;

	@ElementCollection
	@CollectionTable(name = "admin_class_students", joinColumns = @JoinColumn(name = "admin_class_id"))
	@Column(name = "student_id", nullable = false)
	private Set<UUID> studentIds = new HashSet<>();

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

	public MajorEntity getMajor() {
		return major;
	}

	public void setMajor(MajorEntity major) {
		this.major = major;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCohortYear() {
		return cohortYear;
	}

	public void setCohortYear(int cohortYear) {
		this.cohortYear = cohortYear;
	}

	public Set<UUID> getStudentIds() {
		return studentIds;
	}

	public void setStudentIds(Set<UUID> studentIds) {
		this.studentIds = studentIds;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
