package com.unihub.identity.repository;

import com.unihub.identity.domain.AuditLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository
		extends
			JpaRepository<AuditLogEntity, UUID>,
			JpaSpecificationExecutor<AuditLogEntity> {
}
