package com.unihub.learning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.learning.domain.AssignmentEntity;
import com.unihub.learning.domain.SubmissionEntity;
import com.unihub.learning.dto.LearningDtos.GradeSubmissionRequest;
import com.unihub.learning.dto.LearningDtos.SubmissionDto;
import com.unihub.learning.dto.LearningDtos.SubmissionRequest;
import com.unihub.learning.repository.AssignmentRepository;
import com.unihub.learning.repository.SubmissionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssignmentServiceTest {

	private AssignmentRepository assignmentRepo;
	private SubmissionRepository submissionRepo;
	private AssignmentService service;

	@BeforeEach
	void setUp() {
		assignmentRepo = mock(AssignmentRepository.class);
		submissionRepo = mock(SubmissionRepository.class);
		service = new AssignmentService(assignmentRepo, submissionRepo);
	}

	@Test
	void resubmissionIncrementsVersionAndMarksLateWhenAllowed() {
		UUID assignmentId = UUID.randomUUID();
		UUID studentId = UUID.randomUUID();
		AssignmentEntity assignment = new AssignmentEntity();
		assignment.setId(assignmentId);
		assignment.setPublished(true);
		assignment.setAllowLate(true);
		assignment.setDueAt(Instant.now().minusSeconds(3600));
		assignment.setMaxScore(BigDecimal.TEN);

		SubmissionEntity prev = new SubmissionEntity();
		prev.setVersion(1);

		when(assignmentRepo.findById(assignmentId)).thenReturn(Optional.of(assignment));
		when(submissionRepo.findTopByAssignmentIdAndStudentIdOrderByVersionDesc(assignmentId, studentId))
				.thenReturn(Optional.of(prev));
		when(submissionRepo.save(any())).thenAnswer(inv -> {
			SubmissionEntity s = inv.getArgument(0);
			s.setId(UUID.randomUUID());
			return s;
		});

		SubmissionDto dto = service.submit(studentId, assignmentId, new SubmissionRequest("file-123", null));

		assertThat(dto.version()).isEqualTo(2);
		assertThat(dto.late()).isTrue();
	}

	@Test
	void submitRejectsPastDeadlineWhenLateDisallowed() {
		UUID assignmentId = UUID.randomUUID();
		AssignmentEntity assignment = new AssignmentEntity();
		assignment.setId(assignmentId);
		assignment.setPublished(true);
		assignment.setAllowLate(false);
		assignment.setDueAt(Instant.now().minusSeconds(3600));

		when(assignmentRepo.findById(assignmentId)).thenReturn(Optional.of(assignment));

		assertThatThrownBy(() -> service.submit(UUID.randomUUID(), assignmentId, new SubmissionRequest("file-1", null)))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.FORBIDDEN);
	}

	@Test
	void gradeRejectsScoreAboveMaxScore() {
		UUID submissionId = UUID.randomUUID();
		AssignmentEntity assignment = new AssignmentEntity();
		assignment.setMaxScore(BigDecimal.TEN);
		SubmissionEntity submission = new SubmissionEntity();
		submission.setId(submissionId);
		submission.setAssignment(assignment);

		when(submissionRepo.findById(submissionId)).thenReturn(Optional.of(submission));

		assertThatThrownBy(() -> service.grade(UUID.randomUUID(), submissionId,
				new GradeSubmissionRequest(BigDecimal.valueOf(12), "Too high"))).isInstanceOf(AppException.class)
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
	}
}
