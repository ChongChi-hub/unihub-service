package com.unihub.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unihub.academic.domain.FacultyEntity;
import com.unihub.academic.domain.SemesterEntity;
import com.unihub.academic.domain.SubjectEntity;
import com.unihub.academic.dto.AcademicDtos.SemesterDto;
import com.unihub.academic.dto.AcademicDtos.SemesterRequest;
import com.unihub.academic.dto.AcademicDtos.SubjectRequest;
import com.unihub.academic.repository.AdminClassRepository;
import com.unihub.academic.repository.FacultyRepository;
import com.unihub.academic.repository.MajorRepository;
import com.unihub.academic.repository.SemesterRepository;
import com.unihub.academic.repository.SubjectRepository;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AcademicServiceTest {

	@Test
	void deleteFacultyFailsWhenReferencedByMajors() {
		FacultyRepository facultyRepo = mock(FacultyRepository.class);
		MajorRepository majorRepo = mock(MajorRepository.class);
		AdminClassRepository classRepo = mock(AdminClassRepository.class);
		SubjectRepository subjectRepo = mock(SubjectRepository.class);
		AcademicCatalogService service = new AcademicCatalogService(facultyRepo, majorRepo, classRepo, subjectRepo);

		UUID id = UUID.randomUUID();
		when(facultyRepo.findById(id)).thenReturn(Optional.of(new FacultyEntity()));
		when(majorRepo.existsByFacultyId(id)).thenReturn(true);

		assertThatThrownBy(() -> service.deleteFaculty(id)).isInstanceOf(AppException.class)
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
	}

	@Test
	void semesterRejectsOverlappingDatesAndClearsPreviousCurrentFlag() {
		SemesterRepository semesterRepo = mock(SemesterRepository.class);
		SemesterService service = new SemesterService(semesterRepo);

		SemesterEntity existing = new SemesterEntity();
		existing.setId(UUID.randomUUID());
		when(semesterRepo.findOverlapping(any(), any())).thenReturn(List.of(existing));

		assertThatThrownBy(() -> service.create(
				new SemesterRequest("2026-2", "HK2", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 12, 20), true)))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.CONFLICT);

		when(semesterRepo.findOverlapping(any(), any())).thenReturn(List.of());
		when(semesterRepo.existsByCodeIgnoreCase("2026-2")).thenReturn(false);
		when(semesterRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

		SemesterDto created = service
				.create(new SemesterRequest("2026-2", "HK2", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 1), true));

		assertThat(created.current()).isTrue();
		verify(semesterRepo).clearCurrentFlag();
	}

	@Test
	void subjectDetectsCyclicPrerequisites() {
		SubjectRepository subjectRepo = mock(SubjectRepository.class);
		FacultyRepository facultyRepo = mock(FacultyRepository.class);
		SubjectService service = new SubjectService(subjectRepo, facultyRepo);

		UUID facultyId = UUID.randomUUID();
		FacultyEntity faculty = new FacultyEntity();
		faculty.setId(facultyId);

		UUID subAId = UUID.randomUUID();
		UUID subBId = UUID.randomUUID();
		SubjectEntity subA = new SubjectEntity();
		subA.setId(subAId);
		subA.setCode("CS101");
		subA.setFaculty(faculty);

		SubjectEntity subB = new SubjectEntity();
		subB.setId(subBId);
		subB.setCode("CS201");
		subB.setFaculty(faculty);
		subB.setPrerequisites(Set.of(subA));

		when(subjectRepo.findById(subAId)).thenReturn(Optional.of(subA));
		when(subjectRepo.findById(subBId)).thenReturn(Optional.of(subB));
		when(facultyRepo.findById(facultyId)).thenReturn(Optional.of(faculty));

		assertThatThrownBy(
				() -> service.update(subAId, new SubjectRequest(facultyId, "CS101", "Intro", 3, null, Set.of(subBId))))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.BAD_REQUEST);
	}
}
