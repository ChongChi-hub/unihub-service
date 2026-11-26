package com.unihub.academic.repository;

import com.unihub.academic.domain.AdminClassEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminClassRepository
		extends
			JpaRepository<AdminClassEntity, UUID>,
			JpaSpecificationExecutor<AdminClassEntity> {
	boolean existsByCodeIgnoreCase(String code);

	boolean existsByMajorId(UUID majorId);

	Optional<AdminClassEntity> findByCodeIgnoreCase(String code);
}
