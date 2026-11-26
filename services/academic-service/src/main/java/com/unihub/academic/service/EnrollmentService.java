package com.unihub.academic.service;

import com.unihub.academic.client.IdentityClient;
import com.unihub.academic.domain.CourseSectionEntity;
import com.unihub.academic.domain.CourseSectionStatus;
import com.unihub.academic.domain.EnrollmentEntity;
import com.unihub.academic.domain.EnrollmentStatus;
import com.unihub.academic.domain.SemesterEntity;
import com.unihub.academic.domain.SubjectEntity;
import com.unihub.academic.dto.CourseSectionDtos.EnrollmentDto;
import com.unihub.academic.dto.CourseSectionDtos.TimetableSlotDto;
import com.unihub.academic.repository.CourseSectionRepository;
import com.unihub.academic.repository.EnrollmentRepository;
import com.unihub.common.api.ApiResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

	static final int MAX_CREDITS_PER_SEMESTER = 24;

	private final CourseSectionRepository sectionRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final IdentityClient identityClient;

	public EnrollmentService(CourseSectionRepository sectionRepository, EnrollmentRepository enrollmentRepository,
			IdentityClient identityClient) {
		this.sectionRepository = sectionRepository;
		this.enrollmentRepository = enrollmentRepository;
		this.identityClient = identityClient;
	}

	@Transactional
	public EnrollmentDto enroll(UUID studentId, UUID sectionId) {
		CourseSectionEntity section = sectionRepository.findById(sectionId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Course section not found"));

		if (section.getStatus() != CourseSectionStatus.OPEN) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Course section is closed");
		}

		ensureWithinRegistrationWindow(section.getSemester());
		verifyStudentActive(studentId);

		if (section.getEnrolledCount() >= section.getMaxCapacity()) {
			throw new AppException(ErrorCode.CONFLICT, "Course section is full");
		}

		EnrollmentEntity existing = enrollmentRepository.findByCourseSectionIdAndStudentId(sectionId, studentId)
				.orElse(null);
		if (existing != null && existing.getStatus() == EnrollmentStatus.ENROLLED) {
			throw new AppException(ErrorCode.CONFLICT, "Already enrolled in this section");
		}

		UUID semesterId = section.getSemester().getId();
		List<EnrollmentEntity> currentEnrollments = enrollmentRepository.findByStudentAndSemester(studentId, semesterId,
				EnrollmentStatus.ENROLLED);

		// Check prerequisite completion
		Set<UUID> subjectHistory = new HashSet<>(enrollmentRepository.findCompletedOrEnrolledSubjectIds(studentId));
		for (SubjectEntity prereq : section.getSubject().getPrerequisites()) {
			if (!subjectHistory.contains(prereq.getId())) {
				throw new AppException(ErrorCode.BAD_REQUEST, "Missing prerequisite subject: " + prereq.getCode());
			}
		}

		// Check schedule conflict & max credits
		int currentCredits = 0;
		for (EnrollmentEntity enrolled : currentEnrollments) {
			CourseSectionEntity other = enrolled.getCourseSection();
			currentCredits += other.getSubject().getCredits();
			if (CourseSectionService.hasScheduleOverlap(section, other)) {
				throw new AppException(ErrorCode.CONFLICT,
						"Schedule conflicts with enrolled section " + other.getCode());
			}
		}

		int newTotalCredits = currentCredits + section.getSubject().getCredits();
		if (newTotalCredits > MAX_CREDITS_PER_SEMESTER) {
			throw new AppException(ErrorCode.BAD_REQUEST,
					"Exceeds maximum " + MAX_CREDITS_PER_SEMESTER + " credits per semester");
		}

		EnrollmentEntity target = existing != null ? existing : new EnrollmentEntity();
		target.setCourseSection(section);
		target.setStudentId(studentId);
		target.setStatus(EnrollmentStatus.ENROLLED);
		target.setEnrolledAt(Instant.now());
		target.setDroppedAt(null);

		section.setEnrolledCount(section.getEnrolledCount() + 1);
		sectionRepository.save(section);
		return toDto(enrollmentRepository.save(target));
	}

	@Transactional
	public void drop(UUID studentId, UUID sectionId) {
		EnrollmentEntity enrollment = enrollmentRepository.findByCourseSectionIdAndStudentId(sectionId, studentId)
				.filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Enrollment not found"));

		CourseSectionEntity section = enrollment.getCourseSection();
		ensureWithinRegistrationWindow(section.getSemester());

		enrollment.setStatus(EnrollmentStatus.DROPPED);
		enrollment.setDroppedAt(Instant.now());
		enrollmentRepository.save(enrollment);

		section.setEnrolledCount(Math.max(0, section.getEnrolledCount() - 1));
		sectionRepository.save(section);
	}

	@Transactional(readOnly = true)
	public List<TimetableSlotDto> getTimetable(UUID userId, UUID semesterId, boolean isLecturer) {
		List<CourseSectionEntity> sections = isLecturer
				? sectionRepository.findBySemesterAndLecturer(semesterId, userId)
				: enrollmentRepository.findByStudentAndSemester(userId, semesterId, EnrollmentStatus.ENROLLED).stream()
						.map(EnrollmentEntity::getCourseSection).toList();

		return sections.stream()
				.flatMap(sec -> sec.getSchedules().stream()
						.map(sch -> new TimetableSlotDto(sec.getId(), sec.getCode(), sec.getSubject().getCode(),
								sec.getSubject().getName(), sch.getDayOfWeek(), sch.getStartPeriod(),
								sch.getEndPeriod(), sch.getRoom(), Set.copyOf(sec.getLecturerIds()))))
				.sorted(Comparator.comparing(TimetableSlotDto::dayOfWeek).thenComparing(TimetableSlotDto::startPeriod))
				.toList();
	}

	private void verifyStudentActive(UUID studentId) {
		ApiResponse<IdentityClient.RemoteUserDto> response = identityClient.getUserById(studentId);
		if (response == null || response.data() == null || !"ACTIVE".equalsIgnoreCase(response.data().status())) {
			throw new AppException(ErrorCode.FORBIDDEN, "Student account is not active");
		}
	}

	private static void ensureWithinRegistrationWindow(SemesterEntity semester) {
		if (semester.getRegistrationStart() == null || semester.getRegistrationEnd() == null) {
			return;
		}
		Instant now = Instant.now();
		if (now.isBefore(semester.getRegistrationStart()) || now.isAfter(semester.getRegistrationEnd())) {
			throw new AppException(ErrorCode.FORBIDDEN, "Outside course registration window");
		}
	}

	static EnrollmentDto toDto(EnrollmentEntity e) {
		CourseSectionEntity sec = e.getCourseSection();
		return new EnrollmentDto(e.getId(), sec.getId(), sec.getCode(), sec.getSubject().getCode(),
				sec.getSubject().getName(), sec.getSubject().getCredits(), e.getStudentId(), e.getStatus(),
				e.getEnrolledAt());
	}
}
