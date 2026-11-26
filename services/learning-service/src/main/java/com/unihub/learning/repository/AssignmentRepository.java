package com.unihub.learning.repository;

import com.unihub.learning.domain.AssignmentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {
	List<AssignmentEntity> findByCourseSectionIdOrderByDueAtAsc(UUID courseSectionId);
}
