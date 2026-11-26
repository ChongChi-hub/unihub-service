package com.unihub.assessment.repository;

import com.unihub.assessment.domain.GradeComponentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeComponentRepository extends JpaRepository<GradeComponentEntity, UUID> {
	List<GradeComponentEntity> findByCourseSectionId(UUID courseSectionId);
}
