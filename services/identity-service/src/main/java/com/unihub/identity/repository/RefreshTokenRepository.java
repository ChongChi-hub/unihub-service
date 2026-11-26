package com.unihub.identity.repository;

import com.unihub.identity.domain.RefreshTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
	Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

	@Modifying
	@Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId AND r.revoked = false")
	void revokeAllActiveByUserId(UUID userId);
}
