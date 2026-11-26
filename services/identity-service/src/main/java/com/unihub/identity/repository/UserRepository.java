package com.unihub.identity.repository;

import com.unihub.identity.domain.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
	Optional<UserEntity> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByCodeIgnoreCase(String code);
}
