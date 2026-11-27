package com.unihub.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
public class AlertRuleEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(name = "max_absences", nullable = false)
	private int maxAbsences = 3;

	@Column(name = "min_score", nullable = false, precision = 5, scale = 2)
	private BigDecimal minScore = new BigDecimal("4.00");

	@Column(nullable = false)
	private boolean active = true;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxAbsences() {
		return maxAbsences;
	}

	public void setMaxAbsences(int maxAbsences) {
		this.maxAbsences = maxAbsences;
	}

	public BigDecimal getMinScore() {
		return minScore;
	}

	public void setMinScore(BigDecimal minScore) {
		this.minScore = minScore;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
