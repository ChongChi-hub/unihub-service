package com.unihub.identity.repository;

import com.unihub.identity.domain.RoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Short> {
	Optional<RoleEntity> findByName(String name);
}
