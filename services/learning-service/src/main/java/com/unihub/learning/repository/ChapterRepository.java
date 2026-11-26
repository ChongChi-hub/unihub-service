package com.unihub.learning.repository;

import com.unihub.learning.domain.ChapterEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<ChapterEntity, UUID> {
	List<ChapterEntity> findByCourseSectionIdOrderBySortOrderAsc(UUID courseSectionId);
}
