package com.unihub.report.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unihub.report.domain.AcademicAlertEntity;
import com.unihub.report.domain.AlertRuleEntity;
import com.unihub.report.dto.ReportDtos.AcademicAlertDto;
import com.unihub.report.dto.ReportDtos.SummaryUpsertRequest;
import com.unihub.report.repository.AcademicAlertRepository;
import com.unihub.report.repository.AlertRuleRepository;
import com.unihub.report.repository.StudentAcademicSummaryRepository;
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
class ReportServiceTest {

	@Mock
	private StudentAcademicSummaryRepository summaryRepository;
	@Mock
	private AlertRuleRepository ruleRepository;
	@Mock
	private AcademicAlertRepository alertRepository;

	private ReportService reportService;

	@BeforeEach
	void setUp() {
		reportService = new ReportService(summaryRepository, ruleRepository, alertRepository);
	}

	@Test
	void upsertSummaryTriggersAlertWhenScoreBelowRule() {
		UUID studentId = UUID.randomUUID();
		UUID sectionId = UUID.randomUUID();
		AlertRuleEntity rule = new AlertRuleEntity();
		rule.setId(UUID.randomUUID());
		rule.setMinScore(new BigDecimal("4.00"));
		rule.setActive(true);

		when(summaryRepository.findByStudentIdAndCourseSectionId(studentId, sectionId)).thenReturn(Optional.empty());
		when(summaryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(ruleRepository.findByActiveTrue()).thenReturn(List.of(rule));

		reportService.upsertSummary(new SummaryUpsertRequest(studentId, UUID.randomUUID(), sectionId,
				new BigDecimal("90.00"), new BigDecimal("3.20"), new BigDecimal("50.00")));

		verify(alertRepository).save(any(AcademicAlertEntity.class));
	}

	@Test
	void resolveAlertMarksResolved() {
		UUID alertId = UUID.randomUUID();
		AcademicAlertEntity alert = new AcademicAlertEntity();
		alert.setId(alertId);
		alert.setResolved(false);

		when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
		when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		AcademicAlertDto dto = reportService.resolveAlert(alertId);
		assertTrue(dto.resolved());
		assertEquals(alertId, dto.id());
	}
}
