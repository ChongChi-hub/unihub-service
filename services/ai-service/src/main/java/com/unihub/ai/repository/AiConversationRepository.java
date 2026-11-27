package com.unihub.ai.repository;

import com.unihub.ai.domain.AiConversationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConversationRepository extends JpaRepository<AiConversationEntity, UUID> {
	List<AiConversationEntity> findByUserIdAndCourseSectionIdOrderByCreatedAtDesc(UUID userId, UUID courseSectionId);
}
