package com.unihub.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AcademicDtos {

	private AcademicDtos() {
	}

	public record FacultyRequest(@NotBlank @Size(max = 32) String code, @NotBlank @Size(max = 255) String name) {
	}

	public record FacultyDto(UUID id, String code, String name, Instant createdAt) {
	}

	public record MajorRequest(@NotNull UUID facultyId, @NotBlank @Size(max = 32) String code,
			@NotBlank @Size(max = 255) String name) {
	}

	public record MajorDto(UUID id, UUID facultyId, String facultyName, String code, String name, Instant createdAt) {
	}

	public record AdminClassRequest(@NotNull UUID majorId, @NotBlank @Size(max = 32) String code,
			@NotBlank @Size(max = 255) String name, @Min(2000) @Max(2100) int cohortYear) {
	}

	public record AdminClassDto(UUID id, UUID majorId, String majorName, String code, String name, int cohortYear,
			int studentCount, Instant createdAt) {
	}

	public record AssignStudentsRequest(@NotEmpty Set<UUID> studentIds) {
	}

	public record SemesterRequest(@NotBlank @Size(max = 32) String code, @NotBlank @Size(max = 255) String name,
			@NotNull LocalDate startDate, @NotNull LocalDate endDate, boolean current) {
	}

	public record SemesterDto(UUID id, String code, String name, LocalDate startDate, LocalDate endDate,
			boolean current) {
	}

	public record SubjectRequest(@NotNull UUID facultyId, @NotBlank @Size(max = 32) String code,
			@NotBlank @Size(max = 255) String name, @Min(1) @Max(15) int credits, String description,
			Set<UUID> prerequisiteIds) {
	}

	public record SubjectDto(UUID id, UUID facultyId, String facultyName, String code, String name, int credits,
			String description, List<UUID> prerequisiteIds, Instant createdAt) {
	}
}
