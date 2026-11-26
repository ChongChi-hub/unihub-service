package com.unihub.assessment.repository;

import com.unihub.assessment.domain.StudentGradeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGradeRepository extends JpaRepository<StudentGradeEntity, UUID> {
	List<StudentGradeEntity> findByCourseSectionId(UUID courseSectionId);

	List<StudentGradeEntity> findByCourseSectionIdAndStudentId(UUID courseSectionId, UUID studentId);

	Optional<StudentGradeEntity> findByStudentIdAndComponentId(UUID studentId, UUID componentId);
}
