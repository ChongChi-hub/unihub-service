package com.unihub.learning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.unihub.learning.domain.ChapterEntity;
import com.unihub.learning.domain.LessonEntity;
import com.unihub.learning.domain.LessonProgressEntity;
import com.unihub.learning.dto.LearningDtos.ProgressSummaryDto;
import com.unihub.learning.repository.ChapterRepository;
import com.unihub.learning.repository.LessonAttachmentRepository;
import com.unihub.learning.repository.LessonProgressRepository;
import com.unihub.learning.repository.LessonRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LearningServiceTest {

	@Test
	void markLessonCompletedCalculatesProgressPercentage() {
		ChapterRepository chapterRepo = mock(ChapterRepository.class);
		LessonRepository lessonRepo = mock(LessonRepository.class);
		LessonAttachmentRepository attRepo = mock(LessonAttachmentRepository.class);
		LessonProgressRepository progRepo = mock(LessonProgressRepository.class);

		LearningContentService service = new LearningContentService(chapterRepo, lessonRepo, attRepo, progRepo);

		UUID sectionId = UUID.randomUUID();
		UUID studentId = UUID.randomUUID();
		ChapterEntity ch = new ChapterEntity();
		ch.setId(UUID.randomUUID());
		ch.setCourseSectionId(sectionId);

		LessonEntity l1 = new LessonEntity();
		l1.setId(UUID.randomUUID());
		l1.setChapter(ch);
		l1.setPublished(true);

		LessonEntity l2 = new LessonEntity();
		l2.setId(UUID.randomUUID());
		l2.setChapter(ch);
		l2.setPublished(true);

		LessonProgressEntity done1 = new LessonProgressEntity();
		done1.setLessonId(l1.getId());
		done1.setStudentId(studentId);

		when(lessonRepo.findById(l1.getId())).thenReturn(Optional.of(l1));
		when(progRepo.existsById(any())).thenReturn(false);
		when(lessonRepo.findPublishedByCourseSection(sectionId)).thenReturn(List.of(l1, l2));
		when(progRepo.findByStudentIdAndLessonIdIn(any(), anyList())).thenReturn(List.of(done1));

		ProgressSummaryDto summary = service.markLessonCompleted(studentId, l1.getId());

		assertThat(summary.totalLessons()).isEqualTo(2);
		assertThat(summary.completedLessons()).isEqualTo(1);
		assertThat(summary.progressPercent()).isEqualTo(50);
	}
}
