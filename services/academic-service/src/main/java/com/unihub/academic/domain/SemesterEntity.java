package com.unihub.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "semesters")
public class SemesterEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 32)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "is_current", nullable = false)
	private boolean current = false;

	@Column(name = "registration_start")
	private Instant registrationStart;

	@Column(name = "registration_end")
	private Instant registrationEnd;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public Instant getRegistrationStart() {
		return registrationStart;
	}

	public void setRegistrationStart(Instant registrationStart) {
		this.registrationStart = registrationStart;
	}

	public Instant getRegistrationEnd() {
		return registrationEnd;
	}

	public void setRegistrationEnd(Instant registrationEnd) {
		this.registrationEnd = registrationEnd;
	}
}
