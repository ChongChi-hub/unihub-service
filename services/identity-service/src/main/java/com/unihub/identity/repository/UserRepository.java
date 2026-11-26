package com.unihub.identity.repository;

import com.unihub.identity.domain.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
	Optional<UserEntity> findByEmailIgnoreCase(String email);
}
