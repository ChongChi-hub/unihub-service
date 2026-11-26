package com.unihub.academic.dto;

import com.unihub.academic.domain.CourseSectionStatus;
import com.unihub.academic.domain.EnrollmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CourseSectionDtos {

	private CourseSectionDtos() {
	}

	public record ScheduleInput(@Min(2) @Max(8) short dayOfWeek, @Min(1) @Max(15) short startPeriod,
			@Min(1) @Max(15) short endPeriod, String room) {
	}

	public record CourseSectionRequest(@NotNull UUID semesterId, @NotNull UUID subjectId,
			@Min(1) @Max(500) int maxCapacity, String room, @Valid @NotEmpty List<ScheduleInput> schedules) {
	}

	public record ScheduleDto(short dayOfWeek, short startPeriod, short endPeriod, String room) {
	}

	public record CourseSectionDto(UUID id, UUID semesterId, String semesterCode, UUID subjectId, String subjectCode,
			String subjectName, int credits, String code, int maxCapacity, int enrolledCount, String room,
			CourseSectionStatus status, Set<UUID> lecturerIds, List<ScheduleDto> schedules) {
	}

	public record AssignLecturersRequest(@NotEmpty Set<UUID> lecturerIds) {
	}

	public record RegistrationWindowRequest(@NotNull Instant registrationStart, @NotNull Instant registrationEnd) {
	}

	public record ManualEnrollmentRequest(@NotNull UUID studentId, String reason) {
	}

	public record EnrollmentDto(UUID id, UUID courseSectionId, String sectionCode, String subjectCode,
			String subjectName, int credits, UUID studentId, EnrollmentStatus status, Instant enrolledAt) {
	}

	public record TimetableSlotDto(UUID courseSectionId, String sectionCode, String subjectCode, String subjectName,
			short dayOfWeek, short startPeriod, short endPeriod, String room, Set<UUID> lecturerIds) {
	}
}
