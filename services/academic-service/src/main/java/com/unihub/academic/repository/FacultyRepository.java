package com.unihub.academic.repository;

import com.unihub.academic.domain.FacultyEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FacultyRepository extends JpaRepository<FacultyEntity, UUID>, JpaSpecificationExecutor<FacultyEntity> {
	boolean existsByCodeIgnoreCase(String code);

	Optional<FacultyEntity> findByCodeIgnoreCase(String code);
}
