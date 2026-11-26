package com.unihub.learning.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unihub.common.exception.AppException;
import com.unihub.learning.domain.LessonCommentEntity;
import com.unihub.learning.repository.LessonCommentRepository;
import com.unihub.learning.repository.LessonRepository;
import com.unihub.learning.service.LessonCommentService.CreateCommentRequest;
import com.unihub.learning.service.LessonCommentService.LessonCommentDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonCommentServiceTest {

	@Mock
	private LessonCommentRepository commentRepository;
	@Mock
	private LessonRepository lessonRepository;

	private LessonCommentService commentService;

	@BeforeEach
	void setUp() {
		commentService = new LessonCommentService(commentRepository, lessonRepository);
	}

	@Test
	void createCommentRejectsMissingLesson() {
		UUID lessonId = UUID.randomUUID();
		when(lessonRepository.existsById(lessonId)).thenReturn(false);

		assertThrows(AppException.class,
				() -> commentService.createComment(lessonId, UUID.randomUUID(), new CreateCommentRequest(null, "Hi")));
	}

	@Test
	void listCommentsHidesModeratedForStudents() {
		UUID lessonId = UUID.randomUUID();
		LessonCommentEntity visible = new LessonCommentEntity();
		visible.setLessonId(lessonId);
		visible.setContent("Visible");
		visible.setHidden(false);

		LessonCommentEntity hidden = new LessonCommentEntity();
		hidden.setLessonId(lessonId);
		hidden.setContent("Hidden");
		hidden.setHidden(true);

		when(commentRepository.findByLessonIdOrderByCreatedAtAsc(lessonId)).thenReturn(List.of(visible, hidden));

		List<LessonCommentDto> studentView = commentService.listComments(lessonId, false);
		assertEquals(1, studentView.size());
		assertEquals("Visible", studentView.get(0).content());

		List<LessonCommentDto> lecturerView = commentService.listComments(lessonId, true);
		assertEquals(2, lecturerView.size());
	}

	@Test
	void toggleBestAnswerFlipsFlag() {
		UUID commentId = UUID.randomUUID();
		LessonCommentEntity entity = new LessonCommentEntity();
		entity.setId(commentId);
		entity.setBestAnswer(false);
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(entity));
		when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		LessonCommentDto updated = commentService.toggleBestAnswer(commentId);
		assertTrue(updated.bestAnswer());
		assertFalse(updated.hidden());
	}
}
