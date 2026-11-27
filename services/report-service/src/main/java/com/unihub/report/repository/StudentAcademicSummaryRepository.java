package com.unihub.report.repository;

import com.unihub.report.domain.StudentAcademicSummaryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAcademicSummaryRepository extends JpaRepository<StudentAcademicSummaryEntity, UUID> {
	List<StudentAcademicSummaryEntity> findByStudentId(UUID studentId);

	List<StudentAcademicSummaryEntity> findByCourseSectionId(UUID courseSectionId);

	Optional<StudentAcademicSummaryEntity> findByStudentIdAndCourseSectionId(UUID studentId, UUID courseSectionId);
}
