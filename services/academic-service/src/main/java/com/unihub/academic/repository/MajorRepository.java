package com.unihub.academic.repository;

import com.unihub.academic.domain.MajorEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MajorRepository extends JpaRepository<MajorEntity, UUID>, JpaSpecificationExecutor<MajorEntity> {
	boolean existsByCodeIgnoreCase(String code);

	boolean existsByFacultyId(UUID facultyId);

	Optional<MajorEntity> findByCodeIgnoreCase(String code);
}
