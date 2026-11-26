package com.unihub.assessment.repository;

import com.unihub.assessment.domain.AttendanceRecordEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository
		extends
			JpaRepository<AttendanceRecordEntity, AttendanceRecordEntity.AttendanceRecordId> {
	List<AttendanceRecordEntity> findBySessionId(UUID sessionId);
}
