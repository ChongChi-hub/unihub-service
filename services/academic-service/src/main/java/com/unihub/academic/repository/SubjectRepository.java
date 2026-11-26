package com.unihub.academic.repository;

import com.unihub.academic.domain.SubjectEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID>, JpaSpecificationExecutor<SubjectEntity> {
	boolean existsByCodeIgnoreCase(String code);

	boolean existsByFacultyId(UUID facultyId);

	Optional<SubjectEntity> findByCodeIgnoreCase(String code);
}
