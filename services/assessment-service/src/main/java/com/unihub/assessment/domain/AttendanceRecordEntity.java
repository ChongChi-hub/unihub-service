package com.unihub.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
@IdClass(AttendanceRecordEntity.AttendanceRecordId.class)
public class AttendanceRecordEntity {

	@Id
	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Id
	@Column(name = "student_id", nullable = false)
	private UUID studentId;

	@Column(nullable = false, length = 16)
	private String status = "PRESENT";

	@Column(length = 255)
	private String note;

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public UUID getStudentId() {
		return studentId;
	}

	public void setStudentId(UUID studentId) {
		this.studentId = studentId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public static class AttendanceRecordId implements Serializable {
		private UUID sessionId;
		private UUID studentId;

		public AttendanceRecordId() {
		}

		public AttendanceRecordId(UUID sessionId, UUID studentId) {
			this.sessionId = sessionId;
			this.studentId = studentId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof AttendanceRecordId that))
				return false;
			return Objects.equals(sessionId, that.sessionId) && Objects.equals(studentId, that.studentId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(sessionId, studentId);
		}
	}
}
