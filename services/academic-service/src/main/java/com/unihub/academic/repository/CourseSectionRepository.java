package com.unihub.academic.repository;

import com.unihub.academic.domain.CourseSectionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CourseSectionRepository
		extends
			JpaRepository<CourseSectionEntity, UUID>,
			JpaSpecificationExecutor<CourseSectionEntity> {
	long countBySemesterIdAndSubjectId(UUID semesterId, UUID subjectId);

	@Query("SELECT cs FROM CourseSectionEntity cs JOIN cs.lecturerIds l WHERE cs.semester.id = :semesterId AND l = :lecturerId")
	List<CourseSectionEntity> findBySemesterAndLecturer(UUID semesterId, UUID lecturerId);
}
