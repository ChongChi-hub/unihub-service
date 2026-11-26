package com.unihub.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "course_section_schedules")
public class CourseSectionScheduleEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_section_id", nullable = false)
	private CourseSectionEntity courseSection;

	@Column(name = "day_of_week", nullable = false)
	private short dayOfWeek;

	@Column(name = "start_period", nullable = false)
	private short startPeriod;

	@Column(name = "end_period", nullable = false)
	private short endPeriod;

	@Column(length = 64)
	private String room;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
	}

	public boolean overlaps(CourseSectionScheduleEntity other) {
		return this.dayOfWeek == other.dayOfWeek && this.startPeriod <= other.endPeriod
				&& other.startPeriod <= this.endPeriod;
	}

	public UUID getId() {
		return id;
	}

	public CourseSectionEntity getCourseSection() {
		return courseSection;
	}

	public void setCourseSection(CourseSectionEntity courseSection) {
		this.courseSection = courseSection;
	}

	public short getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(short dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public short getStartPeriod() {
		return startPeriod;
	}

	public void setStartPeriod(short startPeriod) {
		this.startPeriod = startPeriod;
	}

	public short getEndPeriod() {
		return endPeriod;
	}

	public void setEndPeriod(short endPeriod) {
		this.endPeriod = endPeriod;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}
}
