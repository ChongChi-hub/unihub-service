package com.unihub.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "quiz_answers")
@IdClass(QuizAnswerEntity.QuizAnswerId.class)
public class QuizAnswerEntity {

	@Id
	@Column(name = "attempt_id", nullable = false)
	private UUID attemptId;

	@Id
	@Column(name = "question_id", nullable = false)
	private UUID questionId;

	@Column(name = "selected_answers_json", nullable = false, columnDefinition = "TEXT")
	private String selectedAnswersJson;

	@Column(name = "is_correct", nullable = false)
	private boolean correct;

	@Column(name = "earned_points", nullable = false, precision = 6, scale = 2)
	private BigDecimal earnedPoints = BigDecimal.ZERO;

	public UUID getAttemptId() {
		return attemptId;
	}

	public void setAttemptId(UUID attemptId) {
		this.attemptId = attemptId;
	}

	public UUID getQuestionId() {
		return questionId;
	}

	public void setQuestionId(UUID questionId) {
		this.questionId = questionId;
	}

	public String getSelectedAnswersJson() {
		return selectedAnswersJson;
	}

	public void setSelectedAnswersJson(String selectedAnswersJson) {
		this.selectedAnswersJson = selectedAnswersJson;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	public BigDecimal getEarnedPoints() {
		return earnedPoints;
	}

	public void setEarnedPoints(BigDecimal earnedPoints) {
		this.earnedPoints = earnedPoints;
	}

	public static class QuizAnswerId implements Serializable {
		private UUID attemptId;
		private UUID questionId;

		public QuizAnswerId() {
		}

		public QuizAnswerId(UUID attemptId, UUID questionId) {
			this.attemptId = attemptId;
			this.questionId = questionId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof QuizAnswerId that))
				return false;
			return Objects.equals(attemptId, that.attemptId) && Objects.equals(questionId, that.questionId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(attemptId, questionId);
		}
	}
}
