package com.unihub.report.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ReportDtos {

	private ReportDtos() {
	}

	public record SummaryUpsertRequest(@NotNull UUID studentId, @NotNull UUID semesterId, @NotNull UUID courseSectionId,
			@NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal attendanceRate,
			@NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal currentScore,
			@NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal progressPercent) {
	}

	public record SummaryDto(UUID id, UUID studentId, UUID semesterId, UUID courseSectionId, BigDecimal attendanceRate,
			BigDecimal currentScore, BigDecimal progressPercent, Instant updatedAt) {
	}

	public record AlertRuleRequest(@NotBlank String name, @Min(1) int maxAbsences,
			@NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal minScore, boolean active) {
	}

	public record AlertRuleDto(UUID id, String name, int maxAbsences, BigDecimal minScore, boolean active) {
	}

	public record AcademicAlertDto(UUID id, UUID studentId, UUID courseSectionId, UUID ruleId, String reason,
			boolean resolved, Instant createdAt) {
	}
}
