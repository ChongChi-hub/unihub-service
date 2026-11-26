package com.unihub.learning.repository;

import com.unihub.learning.domain.LessonEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LessonRepository extends JpaRepository<LessonEntity, UUID> {
	List<LessonEntity> findByChapterIdOrderBySortOrderAsc(UUID chapterId);

	@Query("SELECT l FROM LessonEntity l WHERE l.chapter.courseSectionId = :courseSectionId AND l.published = true ORDER BY l.chapter.sortOrder ASC, l.sortOrder ASC")
	List<LessonEntity> findPublishedByCourseSection(UUID courseSectionId);
}
