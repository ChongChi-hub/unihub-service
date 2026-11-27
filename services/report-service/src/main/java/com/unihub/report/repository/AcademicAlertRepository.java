package com.unihub.report.repository;

import com.unihub.report.domain.AcademicAlertEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicAlertRepository extends JpaRepository<AcademicAlertEntity, UUID> {
	List<AcademicAlertEntity> findByOrderByCreatedAtDesc();

	List<AcademicAlertEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
