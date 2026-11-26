package com.unihub.assessment.service;

import com.unihub.assessment.domain.AttendanceRecordEntity;
import com.unihub.assessment.domain.AttendanceSessionEntity;
import com.unihub.assessment.dto.AssessmentDtos.AttendanceRecordDto;
import com.unihub.assessment.dto.AssessmentDtos.AttendanceRecordInput;
import com.unihub.assessment.dto.AssessmentDtos.AttendanceSessionDto;
import com.unihub.assessment.dto.AssessmentDtos.AttendanceSessionRequest;
import com.unihub.assessment.repository.AttendanceRecordRepository;
import com.unihub.assessment.repository.AttendanceSessionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

	private final AttendanceSessionRepository sessionRepository;
	private final AttendanceRecordRepository recordRepository;

	public AttendanceService(AttendanceSessionRepository sessionRepository,
			AttendanceRecordRepository recordRepository) {
		this.sessionRepository = sessionRepository;
		this.recordRepository = recordRepository;
	}

	@Transactional
	public AttendanceSessionDto createSession(UUID lecturerId, AttendanceSessionRequest req) {
		AttendanceSessionEntity session = new AttendanceSessionEntity();
		session.setCourseSectionId(req.courseSectionId());
		session.setSessionDate(req.sessionDate());
		session.setTopic(req.topic());
		session.setCreatedBy(lecturerId);
		AttendanceSessionEntity saved = sessionRepository.save(session);

		if (req.records() != null) {
			for (AttendanceRecordInput r : req.records()) {
				AttendanceRecordEntity rec = new AttendanceRecordEntity();
				rec.setSessionId(saved.getId());
				rec.setStudentId(r.studentId());
				rec.setStatus(r.status().trim().toUpperCase());
				rec.setNote(r.note());
				recordRepository.save(rec);
			}
		}
		return toDto(saved);
	}

	@Transactional(readOnly = true)
	public List<AttendanceSessionDto> listBySection(UUID courseSectionId) {
		return sessionRepository.findByCourseSectionIdOrderBySessionDateDesc(courseSectionId).stream().map(this::toDto)
				.toList();
	}

	private AttendanceSessionDto toDto(AttendanceSessionEntity s) {
		List<AttendanceRecordDto> records = recordRepository.findBySessionId(s.getId()).stream()
				.map(r -> new AttendanceRecordDto(r.getSessionId(), r.getStudentId(), r.getStatus(), r.getNote()))
				.toList();
		return new AttendanceSessionDto(s.getId(), s.getCourseSectionId(), s.getSessionDate(), s.getTopic(),
				s.getCreatedBy(), s.getCreatedAt(), records);
	}
}
