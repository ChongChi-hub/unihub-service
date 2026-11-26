package com.unihub.learning.service;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.learning.domain.AssignmentEntity;
import com.unihub.learning.domain.SubmissionEntity;
import com.unihub.learning.dto.LearningDtos.AssignmentDto;
import com.unihub.learning.dto.LearningDtos.AssignmentRequest;
import com.unihub.learning.dto.LearningDtos.GradeSubmissionRequest;
import com.unihub.learning.dto.LearningDtos.SubmissionDto;
import com.unihub.learning.dto.LearningDtos.SubmissionRequest;
import com.unihub.learning.repository.AssignmentRepository;
import com.unihub.learning.repository.SubmissionRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AssignmentService {

	private final AssignmentRepository assignmentRepository;
	private final SubmissionRepository submissionRepository;

	public AssignmentService(AssignmentRepository assignmentRepository, SubmissionRepository submissionRepository) {
		this.assignmentRepository = assignmentRepository;
		this.submissionRepository = submissionRepository;
	}

	@Transactional(readOnly = true)
	public List<AssignmentDto> listBySection(UUID courseSectionId, boolean includeDrafts) {
		return assignmentRepository.findByCourseSectionIdOrderByDueAtAsc(courseSectionId).stream()
				.filter(a -> includeDrafts || a.isPublished()).map(AssignmentService::toAssignmentDto).toList();
	}

	@Transactional
	public AssignmentDto create(AssignmentRequest request) {
		AssignmentEntity entity = new AssignmentEntity();
		entity.setCourseSectionId(request.courseSectionId());
		entity.setTitle(request.title().trim());
		entity.setDescription(request.description());
		entity.setAttachmentFileId(request.attachmentFileId());
		entity.setDueAt(request.dueAt());
		entity.setMaxScore(request.maxScore());
		entity.setAllowLate(request.allowLate());
		entity.setPublished(request.published());
		return toAssignmentDto(assignmentRepository.save(entity));
	}

	@Transactional
	public SubmissionDto submit(UUID studentId, UUID assignmentId, SubmissionRequest request) {
		if (!StringUtils.hasText(request.fileId()) && !StringUtils.hasText(request.textContent())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Submission must include a fileId or textContent");
		}
		AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Assignment not found"));

		if (!assignment.isPublished()) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Assignment is not published");
		}

		Instant now = Instant.now();
		boolean isLate = now.isAfter(assignment.getDueAt());
		if (isLate && !assignment.isAllowLate()) {
			throw new AppException(ErrorCode.FORBIDDEN,
					"Assignment deadline has passed and late submission is disabled");
		}

		int nextVersion = submissionRepository
				.findTopByAssignmentIdAndStudentIdOrderByVersionDesc(assignmentId, studentId)
				.map(prev -> prev.getVersion() + 1).orElse(1);

		SubmissionEntity submission = new SubmissionEntity();
		submission.setAssignment(assignment);
		submission.setStudentId(studentId);
		submission.setFileId(request.fileId());
		submission.setTextContent(request.textContent());
		submission.setVersion(nextVersion);
		submission.setLate(isLate);
		submission.setSubmittedAt(now);

		return toSubmissionDto(submissionRepository.save(submission));
	}

	@Transactional(readOnly = true)
	public List<SubmissionDto> listSubmissionsForAssignment(UUID assignmentId) {
		return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId).stream()
				.map(AssignmentService::toSubmissionDto).toList();
	}

	@Transactional(readOnly = true)
	public List<SubmissionDto> listStudentSubmissions(UUID studentId, UUID assignmentId) {
		return submissionRepository.findByAssignmentIdAndStudentIdOrderByVersionDesc(assignmentId, studentId).stream()
				.map(AssignmentService::toSubmissionDto).toList();
	}

	@Transactional
	public SubmissionDto grade(UUID lecturerId, UUID submissionId, GradeSubmissionRequest request) {
		SubmissionEntity submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Submission not found"));

		if (request.score().compareTo(submission.getAssignment().getMaxScore()) > 0) {
			throw new AppException(ErrorCode.BAD_REQUEST,
					"Score cannot exceed maxScore (" + submission.getAssignment().getMaxScore() + ")");
		}

		submission.setScore(request.score());
		submission.setFeedback(request.feedback());
		submission.setGradedBy(lecturerId);
		submission.setGradedAt(Instant.now());
		return toSubmissionDto(submissionRepository.save(submission));
	}

	private static AssignmentDto toAssignmentDto(AssignmentEntity a) {
		return new AssignmentDto(a.getId(), a.getCourseSectionId(), a.getTitle(), a.getDescription(),
				a.getAttachmentFileId(), a.getDueAt(), a.getMaxScore(), a.isAllowLate(), a.isPublished(),
				a.getCreatedAt());
	}

	private static SubmissionDto toSubmissionDto(SubmissionEntity s) {
		return new SubmissionDto(s.getId(), s.getAssignment().getId(), s.getStudentId(), s.getFileId(),
				s.getTextContent(), s.getVersion(), s.isLate(), s.getScore(), s.getFeedback(), s.getSubmittedAt(),
				s.getGradedAt());
	}
}
