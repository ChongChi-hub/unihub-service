package com.unihub.learning.repository;

import com.unihub.learning.domain.SubmissionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {
	Optional<SubmissionEntity> findTopByAssignmentIdAndStudentIdOrderByVersionDesc(UUID assignmentId, UUID studentId);

	List<SubmissionEntity> findByAssignmentIdOrderBySubmittedAtDesc(UUID assignmentId);

	List<SubmissionEntity> findByAssignmentIdAndStudentIdOrderByVersionDesc(UUID assignmentId, UUID studentId);
}
