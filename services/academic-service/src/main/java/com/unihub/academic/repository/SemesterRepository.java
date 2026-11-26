package com.unihub.academic.repository;

import com.unihub.academic.domain.SemesterEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SemesterRepository extends JpaRepository<SemesterEntity, UUID> {
	boolean existsByCodeIgnoreCase(String code);

	Optional<SemesterEntity> findByCodeIgnoreCase(String code);

	@Query("SELECT s FROM SemesterEntity s WHERE s.startDate <= :endDate AND s.endDate >= :startDate")
	List<SemesterEntity> findOverlapping(LocalDate startDate, LocalDate endDate);

	@Modifying
	@Query("UPDATE SemesterEntity s SET s.current = false WHERE s.current = true")
	void clearCurrentFlag();
}
