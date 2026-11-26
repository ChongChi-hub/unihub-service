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
@Table(name = "quiz_questions")
@IdClass(QuizQuestionEntity.QuizQuestionId.class)
public class QuizQuestionEntity {

	@Id
	@Column(name = "quiz_id", nullable = false)
	private UUID quizId;

	@Id
	@Column(name = "question_id", nullable = false)
	private UUID questionId;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(nullable = false, precision = 6, scale = 2)
	private BigDecimal points = BigDecimal.ONE;

	public UUID getQuizId() {
		return quizId;
	}

	public void setQuizId(UUID quizId) {
		this.quizId = quizId;
	}

	public UUID getQuestionId() {
		return questionId;
	}

	public void setQuestionId(UUID questionId) {
		this.questionId = questionId;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public BigDecimal getPoints() {
		return points;
	}

	public void setPoints(BigDecimal points) {
		this.points = points;
	}

	public static class QuizQuestionId implements Serializable {
		private UUID quizId;
		private UUID questionId;

		public QuizQuestionId() {
		}

		public QuizQuestionId(UUID quizId, UUID questionId) {
			this.quizId = quizId;
			this.questionId = questionId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof QuizQuestionId that))
				return false;
			return Objects.equals(quizId, that.quizId) && Objects.equals(questionId, that.questionId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(quizId, questionId);
		}
	}
}
