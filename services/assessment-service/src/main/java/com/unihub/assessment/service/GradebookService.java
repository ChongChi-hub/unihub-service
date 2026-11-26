package com.unihub.assessment.service;

import com.unihub.assessment.domain.GradeComponentEntity;
import com.unihub.assessment.domain.StudentGradeEntity;
import com.unihub.assessment.dto.AssessmentDtos.GradeComponentDto;
import com.unihub.assessment.dto.AssessmentDtos.GradeComponentRequest;
import com.unihub.assessment.dto.AssessmentDtos.StudentGradeDto;
import com.unihub.assessment.dto.AssessmentDtos.StudentGradeRequest;
import com.unihub.assessment.repository.GradeComponentRepository;
import com.unihub.assessment.repository.StudentGradeRepository;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GradebookService {

	private static final BigDecimal HUNDRED = new BigDecimal("100.00");

	private final GradeComponentRepository componentRepository;
	private final StudentGradeRepository gradeRepository;

	public GradebookService(GradeComponentRepository componentRepository, StudentGradeRepository gradeRepository) {
		this.componentRepository = componentRepository;
		this.gradeRepository = gradeRepository;
	}

	@Transactional
	public GradeComponentDto createComponent(GradeComponentRequest req) {
		List<GradeComponentEntity> existing = componentRepository.findByCourseSectionId(req.courseSectionId());
		BigDecimal currentSum = existing.stream().map(GradeComponentEntity::getWeightPercent).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		if (currentSum.add(req.weightPercent()).compareTo(HUNDRED) > 0) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Total grade component weight cannot exceed 100%");
		}
		GradeComponentEntity entity = new GradeComponentEntity();
		entity.setCourseSectionId(req.courseSectionId());
		entity.setName(req.name().trim());
		entity.setWeightPercent(req.weightPercent());
		entity.setLinkedType(req.linkedType() == null || req.linkedType().isBlank()
				? "MANUAL"
				: req.linkedType().trim().toUpperCase());
		entity.setLinkedId(req.linkedId());
		return toComponentDto(componentRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<GradeComponentDto> listComponents(UUID courseSectionId) {
		return componentRepository.findByCourseSectionId(courseSectionId).stream().map(this::toComponentDto).toList();
	}

	@Transactional
	public StudentGradeDto upsertGrade(UUID lecturerId, StudentGradeRequest req) {
		GradeComponentEntity comp = componentRepository.findById(req.componentId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Grade component not found"));
		if (comp.isLocked()) {
			throw new AppException(ErrorCode.CONFLICT, "Grade component is locked");
		}
		StudentGradeEntity entity = gradeRepository.findByStudentIdAndComponentId(req.studentId(), req.componentId())
				.orElseGet(StudentGradeEntity::new);
		if (entity.isLocked()) {
			throw new AppException(ErrorCode.CONFLICT, "Student grade is locked");
		}
		entity.setCourseSectionId(req.courseSectionId());
		entity.setStudentId(req.studentId());
		entity.setComponentId(req.componentId());
		entity.setScore(req.score());
		entity.setUpdatedBy(lecturerId);
		return toGradeDto(gradeRepository.save(entity));
	}

	@Transactional
	public GradeComponentDto lockComponent(UUID componentId) {
		GradeComponentEntity comp = componentRepository.findById(componentId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Grade component not found"));
		comp.setLocked(true);
		return toComponentDto(componentRepository.save(comp));
	}

	@Transactional(readOnly = true)
	public List<StudentGradeDto> listSectionGrades(UUID courseSectionId) {
		return gradeRepository.findByCourseSectionId(courseSectionId).stream().map(this::toGradeDto).toList();
	}

	@Transactional(readOnly = true)
	public List<StudentGradeDto> listMyGrades(UUID courseSectionId, UUID studentId) {
		return gradeRepository.findByCourseSectionIdAndStudentId(courseSectionId, studentId).stream()
				.map(this::toGradeDto).toList();
	}

	private GradeComponentDto toComponentDto(GradeComponentEntity e) {
		return new GradeComponentDto(e.getId(), e.getCourseSectionId(), e.getName(), e.getWeightPercent(),
				e.getLinkedType(), e.getLinkedId(), e.isLocked());
	}

	private StudentGradeDto toGradeDto(StudentGradeEntity e) {
		return new StudentGradeDto(e.getId(), e.getCourseSectionId(), e.getStudentId(), e.getComponentId(),
				e.getScore(), e.isLocked(), e.getUpdatedBy(), e.getUpdatedAt());
	}
}
