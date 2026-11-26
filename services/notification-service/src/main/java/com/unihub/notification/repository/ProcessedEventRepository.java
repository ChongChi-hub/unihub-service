package com.unihub.notification.repository;

import com.unihub.notification.domain.ProcessedEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {
}
