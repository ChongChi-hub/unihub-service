package com.unihub.report.repository;

import com.unihub.report.domain.AlertRuleEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRuleRepository extends JpaRepository<AlertRuleEntity, UUID> {
	List<AlertRuleEntity> findByActiveTrue();
}
