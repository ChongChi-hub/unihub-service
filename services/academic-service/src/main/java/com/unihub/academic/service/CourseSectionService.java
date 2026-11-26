package com.unihub.academic.service;

import com.unihub.academic.domain.CourseSectionEntity;
import com.unihub.academic.domain.CourseSectionScheduleEntity;
import com.unihub.academic.domain.CourseSectionStatus;
import com.unihub.academic.domain.EnrollmentEntity;
import com.unihub.academic.domain.EnrollmentStatus;
import com.unihub.academic.domain.SemesterEntity;
import com.unihub.academic.domain.SubjectEntity;
import com.unihub.academic.dto.CourseSectionDtos.AssignLecturersRequest;
import com.unihub.academic.dto.CourseSectionDtos.CourseSectionDto;
import com.unihub.academic.dto.CourseSectionDtos.CourseSectionRequest;
import com.unihub.academic.dto.CourseSectionDtos.EnrollmentDto;
import com.unihub.academic.dto.CourseSectionDtos.ManualEnrollmentRequest;
import com.unihub.academic.dto.CourseSectionDtos.RegistrationWindowRequest;
import com.unihub.academic.dto.CourseSectionDtos.ScheduleDto;
import com.unihub.academic.dto.CourseSectionDtos.ScheduleInput;
import com.unihub.academic.repository.CourseSectionRepository;
import com.unihub.academic.repository.EnrollmentRepository;
import com.unihub.academic.repository.SemesterRepository;
import com.unihub.academic.repository.SubjectRepository;
import com.unihub.common.api.PageResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseSectionService {

	private final CourseSectionRepository sectionRepository;
	private final SemesterRepository semesterRepository;
	private final SubjectRepository subjectRepository;
	private final EnrollmentRepository enrollmentRepository;

	public CourseSectionService(CourseSectionRepository sectionRepository, SemesterRepository semesterRepository,
			SubjectRepository subjectRepository, EnrollmentRepository enrollmentRepository) {
		this.sectionRepository = sectionRepository;
		this.semesterRepository = semesterRepository;
		this.subjectRepository = subjectRepository;
		this.enrollmentRepository = enrollmentRepository;
	}

	@Transactional(readOnly = true)
	public PageResponse<CourseSectionDto> list(UUID semesterId, UUID subjectId, UUID lecturerId, Pageable pageable) {
		Specification<CourseSectionEntity> spec = (root, cq, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (semesterId != null) {
				predicates.add(cb.equal(root.get("semester").get("id"), semesterId));
			}
			if (subjectId != null) {
				predicates.add(cb.equal(root.get("subject").get("id"), subjectId));
			}
			if (lecturerId != null) {
				predicates.add(cb.isMember(lecturerId, root.get("lecturerIds")));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		return PageResponse.from(sectionRepository.findAll(spec, pageable).map(CourseSectionService::toDto));
	}

	@Transactional
	public CourseSectionDto create(CourseSectionRequest request) {
		SemesterEntity semester = semesterRepository.findById(request.semesterId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Semester not found"));
		SubjectEntity subject = subjectRepository.findById(request.subjectId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Subject not found"));

		long seq = sectionRepository.countBySemesterIdAndSubjectId(semester.getId(), subject.getId()) + 1;
		String generatedCode = subject.getCode() + "-" + semester.getCode() + "-" + String.format("%02d", seq);

		CourseSectionEntity section = new CourseSectionEntity();
		section.setSemester(semester);
		section.setSubject(subject);
		section.setCode(generatedCode);
		section.setMaxCapacity(request.maxCapacity());
		section.setRoom(request.room());
		section.setStatus(CourseSectionStatus.OPEN);
		applySchedules(section, request.schedules());

		return toDto(sectionRepository.save(section));
	}

	@Transactional
	public CourseSectionDto update(UUID id, CourseSectionRequest request) {
		CourseSectionEntity section = findSection(id);
		if (request.maxCapacity() < section.getEnrolledCount()) {
			throw new AppException(ErrorCode.BAD_REQUEST, "maxCapacity cannot be less than enrolledCount");
		}
		section.setMaxCapacity(request.maxCapacity());
		section.setRoom(request.room());
		applySchedules(section, request.schedules());
		return toDto(sectionRepository.save(section));
	}

	@Transactional
	public CourseSectionDto close(UUID id) {
		CourseSectionEntity section = findSection(id);
		section.setStatus(CourseSectionStatus.CLOSED);
		return toDto(sectionRepository.save(section));
	}

	@Transactional
	public CourseSectionDto assignLecturers(UUID sectionId, AssignLecturersRequest request) {
		CourseSectionEntity target = findSection(sectionId);
		UUID semesterId = target.getSemester().getId();

		for (UUID lecturerId : request.lecturerIds()) {
			List<CourseSectionEntity> teaching = sectionRepository.findBySemesterAndLecturer(semesterId, lecturerId);
			for (CourseSectionEntity other : teaching) {
				if (other.getId().equals(sectionId)) {
					continue;
				}
				if (hasScheduleOverlap(target, other)) {
					throw new AppException(ErrorCode.CONFLICT,
							"Lecturer " + lecturerId + " has schedule conflict with section " + other.getCode());
				}
			}
		}

		target.getLecturerIds().clear();
		target.getLecturerIds().addAll(request.lecturerIds());
		return toDto(sectionRepository.save(target));
	}

	@Transactional
	public void configureRegistrationWindow(UUID semesterId, RegistrationWindowRequest request) {
		if (!request.registrationEnd().isAfter(request.registrationStart())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "registrationEnd must be after registrationStart");
		}
		SemesterEntity semester = semesterRepository.findById(semesterId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Semester not found"));
		semester.setRegistrationStart(request.registrationStart());
		semester.setRegistrationEnd(request.registrationEnd());
		semesterRepository.save(semester);
	}

	@Transactional(readOnly = true)
	public List<EnrollmentDto> listSectionStudents(UUID sectionId) {
		findSection(sectionId);
		return enrollmentRepository.findByCourseSectionIdAndStatus(sectionId, EnrollmentStatus.ENROLLED).stream()
				.map(EnrollmentService::toDto).toList();
	}

	@Transactional
	public EnrollmentDto adminAddStudent(UUID sectionId, ManualEnrollmentRequest request) {
		CourseSectionEntity section = findSection(sectionId);
		EnrollmentEntity enrollment = enrollmentRepository
				.findByCourseSectionIdAndStudentId(sectionId, request.studentId()).orElseGet(EnrollmentEntity::new);

		if (enrollment.getId() != null && enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
			throw new AppException(ErrorCode.CONFLICT, "Student is already enrolled in this section");
		}
		enrollment.setCourseSection(section);
		enrollment.setStudentId(request.studentId());
		enrollment.setStatus(EnrollmentStatus.ENROLLED);
		enrollment.setEnrolledAt(Instant.now());
		enrollment.setDroppedAt(null);
		section.setEnrolledCount(section.getEnrolledCount() + 1);
		sectionRepository.save(section);
		return EnrollmentService.toDto(enrollmentRepository.save(enrollment));
	}

	@Transactional
	public void adminRemoveStudent(UUID sectionId, UUID studentId) {
		CourseSectionEntity section = findSection(sectionId);
		EnrollmentEntity enrollment = enrollmentRepository.findByCourseSectionIdAndStudentId(sectionId, studentId)
				.filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Enrollment not found"));

		enrollment.setStatus(EnrollmentStatus.DROPPED);
		enrollment.setDroppedAt(Instant.now());
		enrollmentRepository.save(enrollment);
		section.setEnrolledCount(Math.max(0, section.getEnrolledCount() - 1));
		sectionRepository.save(section);
	}

	@Transactional(readOnly = true)
	public byte[] exportSectionRosterExcel(UUID sectionId) {
		CourseSectionEntity section = findSection(sectionId);
		List<EnrollmentEntity> students = enrollmentRepository.findByCourseSectionIdAndStatus(sectionId,
				EnrollmentStatus.ENROLLED);

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet(section.getCode());
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue("STT");
			header.createCell(1).setCellValue("Mã sinh viên (UUID)");
			header.createCell(2).setCellValue("Mã lớp học phần");
			header.createCell(3).setCellValue("Thời gian đăng ký");

			int rowIdx = 1;
			for (EnrollmentEntity e : students) {
				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(rowIdx - 1);
				row.createCell(1).setCellValue(e.getStudentId().toString());
				row.createCell(2).setCellValue(section.getCode());
				row.createCell(3).setCellValue(e.getEnrolledAt().toString());
			}
			workbook.write(out);
			return out.toByteArray();
		} catch (IOException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to export roster Excel");
		}
	}

	private CourseSectionEntity findSection(UUID id) {
		return sectionRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Course section not found"));
	}

	private static void applySchedules(CourseSectionEntity section, List<ScheduleInput> scheduleInputs) {
		section.getSchedules().clear();
		for (ScheduleInput input : scheduleInputs) {
			if (input.endPeriod() < input.startPeriod()) {
				throw new AppException(ErrorCode.BAD_REQUEST, "endPeriod must be >= startPeriod");
			}
			CourseSectionScheduleEntity schedule = new CourseSectionScheduleEntity();
			schedule.setCourseSection(section);
			schedule.setDayOfWeek(input.dayOfWeek());
			schedule.setStartPeriod(input.startPeriod());
			schedule.setEndPeriod(input.endPeriod());
			schedule.setRoom(input.room() != null ? input.room() : section.getRoom());
			section.getSchedules().add(schedule);
		}
	}

	static boolean hasScheduleOverlap(CourseSectionEntity a, CourseSectionEntity b) {
		for (CourseSectionScheduleEntity sa : a.getSchedules()) {
			for (CourseSectionScheduleEntity sb : b.getSchedules()) {
				if (sa.overlaps(sb)) {
					return true;
				}
			}
		}
		return false;
	}

	static CourseSectionDto toDto(CourseSectionEntity e) {
		List<ScheduleDto> schedules = e.getSchedules().stream()
				.map(s -> new ScheduleDto(s.getDayOfWeek(), s.getStartPeriod(), s.getEndPeriod(), s.getRoom()))
				.toList();
		return new CourseSectionDto(e.getId(), e.getSemester().getId(), e.getSemester().getCode(),
				e.getSubject().getId(), e.getSubject().getCode(), e.getSubject().getName(), e.getSubject().getCredits(),
				e.getCode(), e.getMaxCapacity(), e.getEnrolledCount(), e.getRoom(), e.getStatus(),
				Set.copyOf(e.getLecturerIds()), schedules);
	}
}
