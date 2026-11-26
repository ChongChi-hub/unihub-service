package com.unihub.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.unihub.academic.domain.CourseSectionEntity;
import com.unihub.academic.domain.CourseSectionScheduleEntity;
import com.unihub.academic.domain.SemesterEntity;
import com.unihub.academic.domain.SubjectEntity;
import com.unihub.academic.dto.CourseSectionDtos.AssignLecturersRequest;
import com.unihub.academic.repository.CourseSectionRepository;
import com.unihub.academic.repository.EnrollmentRepository;
import com.unihub.academic.repository.SemesterRepository;
import com.unihub.academic.repository.SubjectRepository;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseSectionServiceTest {

	@Test
	void assignLecturersRejectsScheduleConflictAndNamesConflictingSection() {
		CourseSectionRepository sectionRepo = mock(CourseSectionRepository.class);
		CourseSectionService service = new CourseSectionService(sectionRepo, mock(SemesterRepository.class),
				mock(SubjectRepository.class), mock(EnrollmentRepository.class));

		UUID semesterId = UUID.randomUUID();
		UUID lecturerId = UUID.randomUUID();
		SemesterEntity sem = new SemesterEntity();
		sem.setId(semesterId);
		SubjectEntity sub = new SubjectEntity();
		sub.setId(UUID.randomUUID());

		CourseSectionEntity target = sectionWithSchedule(UUID.randomUUID(), sem, sub, "CS101-02", (short) 3, (short) 1,
				(short) 3);
		CourseSectionEntity existing = sectionWithSchedule(UUID.randomUUID(), sem, sub, "CS101-01", (short) 3,
				(short) 2, (short) 4);

		when(sectionRepo.findById(target.getId())).thenReturn(Optional.of(target));
		when(sectionRepo.findBySemesterAndLecturer(semesterId, lecturerId)).thenReturn(List.of(existing));

		assertThatThrownBy(
				() -> service.assignLecturers(target.getId(), new AssignLecturersRequest(Set.of(lecturerId))))
				.isInstanceOf(AppException.class).hasMessageContaining("CS101-01")
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
	}

	@Test
	void exportRosterGeneratesValidXlsxBytes() {
		CourseSectionRepository sectionRepo = mock(CourseSectionRepository.class);
		EnrollmentRepository enrollmentRepo = mock(EnrollmentRepository.class);
		CourseSectionService service = new CourseSectionService(sectionRepo, mock(SemesterRepository.class),
				mock(SubjectRepository.class), enrollmentRepo);

		UUID sectionId = UUID.randomUUID();
		CourseSectionEntity sec = new CourseSectionEntity();
		sec.setId(sectionId);
		sec.setCode("CS101-01");
		when(sectionRepo.findById(sectionId)).thenReturn(Optional.of(sec));
		when(enrollmentRepo.findByCourseSectionIdAndStatus(sectionId,
				com.unihub.academic.domain.EnrollmentStatus.ENROLLED)).thenReturn(List.of());

		byte[] bytes = service.exportSectionRosterExcel(sectionId);
		assertThat(bytes.length).isGreaterThan(100);
	}

	private static CourseSectionEntity sectionWithSchedule(UUID id, SemesterEntity sem, SubjectEntity sub, String code,
			short day, short start, short end) {
		CourseSectionEntity sec = new CourseSectionEntity();
		sec.setId(id);
		sec.setSemester(sem);
		sec.setSubject(sub);
		sec.setCode(code);
		CourseSectionScheduleEntity sch = new CourseSectionScheduleEntity();
		sch.setCourseSection(sec);
		sch.setDayOfWeek(day);
		sch.setStartPeriod(start);
		sch.setEndPeriod(end);
		sec.getSchedules().add(sch);
		return sec;
	}
}
