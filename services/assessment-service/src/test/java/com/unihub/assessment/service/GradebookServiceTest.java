package com.unihub.assessment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.unihub.assessment.domain.GradeComponentEntity;
import com.unihub.assessment.dto.AssessmentDtos.GradeComponentRequest;
import com.unihub.assessment.dto.AssessmentDtos.StudentGradeRequest;
import com.unihub.assessment.repository.GradeComponentRepository;
import com.unihub.assessment.repository.StudentGradeRepository;
import com.unihub.common.exception.AppException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradebookServiceTest {

	@Mock
	private GradeComponentRepository componentRepository;
	@Mock
	private StudentGradeRepository gradeRepository;

	private GradebookService gradebookService;

	@BeforeEach
	void setUp() {
		gradebookService = new GradebookService(componentRepository, gradeRepository);
	}

	@Test
	void createComponentRejectsWeightAbove100() {
		UUID sectionId = UUID.randomUUID();
		GradeComponentEntity existing = new GradeComponentEntity();
		existing.setWeightPercent(new BigDecimal("60.00"));
		when(componentRepository.findByCourseSectionId(sectionId)).thenReturn(List.of(existing));

		assertThrows(AppException.class, () -> gradebookService.createComponent(
				new GradeComponentRequest(sectionId, "Final", new BigDecimal("50.00"), "MANUAL", null)));
	}

	@Test
	void upsertGradeRejectsLockedComponent() {
		UUID compId = UUID.randomUUID();
		GradeComponentEntity comp = new GradeComponentEntity();
		comp.setId(compId);
		comp.setLocked(true);
		when(componentRepository.findById(compId)).thenReturn(Optional.of(comp));

		AppException ex = assertThrows(AppException.class, () -> gradebookService.upsertGrade(UUID.randomUUID(),
				new StudentGradeRequest(UUID.randomUUID(), UUID.randomUUID(), compId, new BigDecimal("8.5"))));
		assertEquals("Grade component is locked", ex.getMessage());
	}
}
