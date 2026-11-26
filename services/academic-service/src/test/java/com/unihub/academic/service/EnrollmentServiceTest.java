package com.unihub.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.unihub.academic.client.IdentityClient;
import com.unihub.academic.domain.CourseSectionEntity;
import com.unihub.academic.domain.CourseSectionScheduleEntity;
import com.unihub.academic.domain.CourseSectionStatus;
import com.unihub.academic.domain.EnrollmentEntity;
import com.unihub.academic.domain.SemesterEntity;
import com.unihub.academic.domain.SubjectEntity;
import com.unihub.academic.dto.CourseSectionDtos.EnrollmentDto;
import com.unihub.academic.repository.CourseSectionRepository;
import com.unihub.academic.repository.EnrollmentRepository;
import com.unihub.common.api.ApiResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrollmentServiceTest {

	private CourseSectionRepository sectionRepo;
	private EnrollmentRepository enrollmentRepo;
	private IdentityClient identityClient;
	private EnrollmentService service;
	private UUID studentId;

	@BeforeEach
	void setUp() {
		sectionRepo = mock(CourseSectionRepository.class);
		enrollmentRepo = mock(EnrollmentRepository.class);
		identityClient = mock(IdentityClient.class);
		service = new EnrollmentService(sectionRepo, enrollmentRepo, identityClient);
		studentId = UUID.randomUUID();

		when(identityClient.getUserById(studentId))
				.thenReturn(ApiResponse.ok(new IdentityClient.RemoteUserDto(studentId, "sv@unihub.edu.vn", "Student",
						"SV01", "ACTIVE", List.of("STUDENT"))));
	}

	@Test
	void enrollSucceedsWhenAllRulesPass() {
		CourseSectionEntity section = buildSection("CS101-01", 3, 40, 10, (short) 2, (short) 1, (short) 3);
		when(sectionRepo.findById(section.getId())).thenReturn(Optional.of(section));
		when(enrollmentRepo.findByStudentAndSemester(any(), any(), any())).thenReturn(List.of());
		when(enrollmentRepo.findCompletedOrEnrolledSubjectIds(studentId)).thenReturn(List.of());
		when(enrollmentRepo.save(any())).thenAnswer(inv -> {
			EnrollmentEntity e = inv.getArgument(0);
			e.setId(UUID.randomUUID());
			return e;
		});

		EnrollmentDto dto = service.enroll(studentId, section.getId());

		assertThat(dto.sectionCode()).isEqualTo("CS101-01");
		assertThat(section.getEnrolledCount()).isEqualTo(11);
	}

	@Test
	void enrollRejectsWhenSectionIsFull() {
		CourseSectionEntity section = buildSection("CS101-01", 3, 40, 40, (short) 2, (short) 1, (short) 3);
		when(sectionRepo.findById(section.getId())).thenReturn(Optional.of(section));

		assertThatThrownBy(() -> service.enroll(studentId, section.getId())).isInstanceOf(AppException.class)
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
	}

	@Test
	void enrollRejectsWhenMissingPrerequisite() {
		CourseSectionEntity section = buildSection("CS201-01", 3, 40, 5, (short) 2, (short) 1, (short) 3);
		SubjectEntity prereq = new SubjectEntity();
		prereq.setId(UUID.randomUUID());
		prereq.setCode("CS101");
		section.getSubject().setPrerequisites(Set.of(prereq));

		when(sectionRepo.findById(section.getId())).thenReturn(Optional.of(section));
		when(enrollmentRepo.findCompletedOrEnrolledSubjectIds(studentId)).thenReturn(List.of());

		assertThatThrownBy(() -> service.enroll(studentId, section.getId())).isInstanceOf(AppException.class)
				.hasMessageContaining("CS101");
	}

	@Test
	void enrollRejectsWhenScheduleOverlapsOrExceedsMaxCredits() {
		CourseSectionEntity target = buildSection("CS201-01", 4, 40, 5, (short) 2, (short) 2, (short) 4);
		CourseSectionEntity existingSec = buildSection("MA101-01", 21, 40, 5, (short) 2, (short) 3, (short) 5);
		EnrollmentEntity existing = new EnrollmentEntity();
		existing.setCourseSection(existingSec);

		when(sectionRepo.findById(target.getId())).thenReturn(Optional.of(target));
		when(enrollmentRepo.findByStudentAndSemester(any(), any(), any())).thenReturn(List.of(existing));

		assertThatThrownBy(() -> service.enroll(studentId, target.getId())).isInstanceOf(AppException.class)
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
	}

	@Test
	void enrollRejectsOutsideRegistrationWindow() {
		CourseSectionEntity section = buildSection("CS101-01", 3, 40, 5, (short) 2, (short) 1, (short) 3);
		section.getSemester().setRegistrationStart(Instant.now().minusSeconds(7200));
		section.getSemester().setRegistrationEnd(Instant.now().minusSeconds(3600));

		when(sectionRepo.findById(section.getId())).thenReturn(Optional.of(section));

		assertThatThrownBy(() -> service.enroll(studentId, section.getId())).isInstanceOf(AppException.class)
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
	}

	private static CourseSectionEntity buildSection(String code, int credits, int maxCap, int enrolled, short day,
			short start, short end) {
		SemesterEntity sem = new SemesterEntity();
		sem.setId(UUID.randomUUID());
		sem.setCode("2026-1");

		SubjectEntity sub = new SubjectEntity();
		sub.setId(UUID.randomUUID());
		sub.setCode(code.split("-")[0]);
		sub.setName("Subject " + code);
		sub.setCredits(credits);

		CourseSectionEntity sec = new CourseSectionEntity();
		sec.setId(UUID.randomUUID());
		sec.setSemester(sem);
		sec.setSubject(sub);
		sec.setCode(code);
		sec.setMaxCapacity(maxCap);
		sec.setEnrolledCount(enrolled);
		sec.setStatus(CourseSectionStatus.OPEN);

		CourseSectionScheduleEntity sch = new CourseSectionScheduleEntity();
		sch.setCourseSection(sec);
		sch.setDayOfWeek(day);
		sch.setStartPeriod(start);
		sch.setEndPeriod(end);
		sec.getSchedules().add(sch);
		return sec;
	}
}
