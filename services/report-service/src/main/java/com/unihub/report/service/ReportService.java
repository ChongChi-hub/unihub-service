package com.unihub.report.service;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.report.domain.AcademicAlertEntity;
import com.unihub.report.domain.AlertRuleEntity;
import com.unihub.report.domain.StudentAcademicSummaryEntity;
import com.unihub.report.dto.ReportDtos.AcademicAlertDto;
import com.unihub.report.dto.ReportDtos.AlertRuleDto;
import com.unihub.report.dto.ReportDtos.AlertRuleRequest;
import com.unihub.report.dto.ReportDtos.SummaryDto;
import com.unihub.report.dto.ReportDtos.SummaryUpsertRequest;
import com.unihub.report.repository.AcademicAlertRepository;
import com.unihub.report.repository.AlertRuleRepository;
import com.unihub.report.repository.StudentAcademicSummaryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

	private static final BigDecimal MIN_ATTENDANCE_RATE = new BigDecimal("80.00");

	private final StudentAcademicSummaryRepository summaryRepository;
	private final AlertRuleRepository ruleRepository;
	private final AcademicAlertRepository alertRepository;

	public ReportService(StudentAcademicSummaryRepository summaryRepository, AlertRuleRepository ruleRepository,
			AcademicAlertRepository alertRepository) {
		this.summaryRepository = summaryRepository;
		this.ruleRepository = ruleRepository;
		this.alertRepository = alertRepository;
	}

	@Transactional
	public SummaryDto upsertSummary(SummaryUpsertRequest req) {
		StudentAcademicSummaryEntity entity = summaryRepository
				.findByStudentIdAndCourseSectionId(req.studentId(), req.courseSectionId())
				.orElseGet(StudentAcademicSummaryEntity::new);
		entity.setStudentId(req.studentId());
		entity.setSemesterId(req.semesterId());
		entity.setCourseSectionId(req.courseSectionId());
		entity.setAttendanceRate(req.attendanceRate());
		entity.setCurrentScore(req.currentScore());
		entity.setProgressPercent(req.progressPercent());
		StudentAcademicSummaryEntity saved = summaryRepository.save(entity);

		evaluateAlerts(saved);
		return toSummaryDto(saved);
	}

	private void evaluateAlerts(StudentAcademicSummaryEntity summary) {
		List<AlertRuleEntity> rules = ruleRepository.findByActiveTrue();
		for (AlertRuleEntity rule : rules) {
			boolean lowScore = summary.getCurrentScore().compareTo(rule.getMinScore()) < 0;
			boolean lowAttendance = summary.getAttendanceRate().compareTo(MIN_ATTENDANCE_RATE) < 0;
			if (lowScore || lowAttendance) {
				AcademicAlertEntity alert = new AcademicAlertEntity();
				alert.setStudentId(summary.getStudentId());
				alert.setCourseSectionId(summary.getCourseSectionId());
				alert.setRuleId(rule.getId());
				alert.setReason(lowScore
						? "Điểm hiện tại (" + summary.getCurrentScore() + ") thấp hơn ngưỡng (" + rule.getMinScore()
								+ ")"
						: "Tỷ lệ chuyên cần (" + summary.getAttendanceRate() + "%) dưới 80%");
				alertRepository.save(alert);
			}
		}
	}

	@Transactional(readOnly = true)
	public List<SummaryDto> listByStudent(UUID studentId) {
		return summaryRepository.findByStudentId(studentId).stream().map(this::toSummaryDto).toList();
	}

	@Transactional(readOnly = true)
	public List<SummaryDto> listBySection(UUID courseSectionId) {
		return summaryRepository.findByCourseSectionId(courseSectionId).stream().map(this::toSummaryDto).toList();
	}

	@Transactional
	public AlertRuleDto createRule(AlertRuleRequest req) {
		AlertRuleEntity rule = new AlertRuleEntity();
		rule.setName(req.name().trim());
		rule.setMaxAbsences(req.maxAbsences());
		rule.setMinScore(req.minScore());
		rule.setActive(req.active());
		return toRuleDto(ruleRepository.save(rule));
	}

	@Transactional(readOnly = true)
	public List<AlertRuleDto> listRules() {
		return ruleRepository.findAll().stream().map(this::toRuleDto).toList();
	}

	@Transactional(readOnly = true)
	public List<AcademicAlertDto> listAlerts() {
		return alertRepository.findByOrderByCreatedAtDesc().stream().map(this::toAlertDto).toList();
	}

	@Transactional(readOnly = true)
	public List<AcademicAlertDto> listMyAlerts(UUID studentId) {
		return alertRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream().map(this::toAlertDto).toList();
	}

	@Transactional
	public AcademicAlertDto resolveAlert(UUID alertId) {
		AcademicAlertEntity alert = alertRepository.findById(alertId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Alert not found"));
		alert.setResolved(true);
		return toAlertDto(alertRepository.save(alert));
	}

	private SummaryDto toSummaryDto(StudentAcademicSummaryEntity e) {
		return new SummaryDto(e.getId(), e.getStudentId(), e.getSemesterId(), e.getCourseSectionId(),
				e.getAttendanceRate(), e.getCurrentScore(), e.getProgressPercent(), e.getUpdatedAt());
	}

	private AlertRuleDto toRuleDto(AlertRuleEntity e) {
		return new AlertRuleDto(e.getId(), e.getName(), e.getMaxAbsences(), e.getMinScore(), e.isActive());
	}

	private AcademicAlertDto toAlertDto(AcademicAlertEntity e) {
		return new AcademicAlertDto(e.getId(), e.getStudentId(), e.getCourseSectionId(), e.getRuleId(), e.getReason(),
				e.isResolved(), e.getCreatedAt());
	}
}
