package com.unihub.ai.repository;

import com.unihub.ai.domain.AiMessageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessageEntity, UUID> {
	List<AiMessageEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
