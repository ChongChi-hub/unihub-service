package com.unihub.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

	@Id
	private UUID id;

	@Column(name = "actor_id")
	private UUID actorId;

	@Column(name = "actor_email")
	private String actorEmail;

	@Column(nullable = false, length = 100)
	private String action;

	@Column(name = "target_type", length = 100)
	private String targetType;

	@Column(name = "target_id", length = 128)
	private String targetId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "before_data", columnDefinition = "jsonb")
	private String beforeData;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "after_data", columnDefinition = "jsonb")
	private String afterData;

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

	public UUID getActorId() {
		return actorId;
	}

	public void setActorId(UUID actorId) {
		this.actorId = actorId;
	}

	public String getActorEmail() {
		return actorEmail;
	}

	public void setActorEmail(String actorEmail) {
		this.actorEmail = actorEmail;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getBeforeData() {
		return beforeData;
	}

	public void setBeforeData(String beforeData) {
		this.beforeData = beforeData;
	}

	public String getAfterData() {
		return afterData;
	}

	public void setAfterData(String afterData) {
		this.afterData = afterData;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
