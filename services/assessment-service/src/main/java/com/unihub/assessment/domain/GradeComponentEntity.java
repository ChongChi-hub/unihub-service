package com.unihub.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "grade_components")
public class GradeComponentEntity {

	@Id
	private UUID id;

	@Column(name = "course_section_id", nullable = false)
	private UUID courseSectionId;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(name = "weight_percent", nullable = false, precision = 5, scale = 2)
	private BigDecimal weightPercent;

	@Column(name = "linked_type", nullable = false, length = 32)
	private String linkedType = "MANUAL";

	@Column(name = "linked_id")
	private UUID linkedId;

	@Column(nullable = false)
	private boolean locked;

	@PrePersist
	void prePersist() {
		if (id == null)
			id = UUID.randomUUID();
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getWeightPercent() {
		return weightPercent;
	}

	public void setWeightPercent(BigDecimal weightPercent) {
		this.weightPercent = weightPercent;
	}

	public String getLinkedType() {
		return linkedType;
	}

	public void setLinkedType(String linkedType) {
		this.linkedType = linkedType;
	}

	public UUID getLinkedId() {
		return linkedId;
	}

	public void setLinkedId(UUID linkedId) {
		this.linkedId = linkedId;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
