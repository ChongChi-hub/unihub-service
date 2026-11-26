package com.unihub.assessment.repository;

import com.unihub.assessment.domain.AttendanceSessionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSessionEntity, UUID> {
	List<AttendanceSessionEntity> findByCourseSectionIdOrderBySessionDateDesc(UUID courseSectionId);
}
