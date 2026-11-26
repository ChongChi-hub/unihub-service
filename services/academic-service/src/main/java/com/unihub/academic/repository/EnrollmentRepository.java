package com.unihub.academic.repository;

import com.unihub.academic.domain.EnrollmentEntity;
import com.unihub.academic.domain.EnrollmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {
	Optional<EnrollmentEntity> findByCourseSectionIdAndStudentId(UUID courseSectionId, UUID studentId);

	List<EnrollmentEntity> findByCourseSectionIdAndStatus(UUID courseSectionId, EnrollmentStatus status);

	@Query("SELECT e FROM EnrollmentEntity e JOIN FETCH e.courseSection cs JOIN FETCH cs.subject WHERE e.studentId = :studentId AND cs.semester.id = :semesterId AND e.status = :status")
	List<EnrollmentEntity> findByStudentAndSemester(UUID studentId, UUID semesterId, EnrollmentStatus status);

	@Query("SELECT DISTINCT cs.subject.id FROM EnrollmentEntity e JOIN e.courseSection cs WHERE e.studentId = :studentId AND e.status = 'ENROLLED'")
	List<UUID> findCompletedOrEnrolledSubjectIds(UUID studentId);
}
