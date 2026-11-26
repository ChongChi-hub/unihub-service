package com.unihub.learning.service;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.learning.domain.LessonCommentEntity;
import com.unihub.learning.repository.LessonCommentRepository;
import com.unihub.learning.repository.LessonRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonCommentService {

	private final LessonCommentRepository commentRepository;
	private final LessonRepository lessonRepository;

	public LessonCommentService(LessonCommentRepository commentRepository, LessonRepository lessonRepository) {
		this.commentRepository = commentRepository;
		this.lessonRepository = lessonRepository;
	}

	@Transactional
	public LessonCommentDto createComment(UUID lessonId, UUID authorId, CreateCommentRequest req) {
		if (!lessonRepository.existsById(lessonId)) {
			throw new AppException(ErrorCode.NOT_FOUND, "Lesson not found");
		}
		if (req.parentId() != null) {
			LessonCommentEntity parent = commentRepository.findById(req.parentId())
					.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Parent comment not found"));
			if (!parent.getLessonId().equals(lessonId)) {
				throw new AppException(ErrorCode.BAD_REQUEST, "Parent comment belongs to another lesson");
			}
		}
		LessonCommentEntity entity = new LessonCommentEntity();
		entity.setLessonId(lessonId);
		entity.setParentId(req.parentId());
		entity.setAuthorId(authorId);
		entity.setContent(req.content().trim());
		return toDto(commentRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<LessonCommentDto> listComments(UUID lessonId, boolean includeHidden) {
		return commentRepository.findByLessonIdOrderByCreatedAtAsc(lessonId).stream()
				.filter(c -> includeHidden || !c.isHidden()).map(this::toDto).toList();
	}

	@Transactional
	public LessonCommentDto toggleBestAnswer(UUID commentId) {
		LessonCommentEntity entity = commentRepository.findById(commentId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Comment not found"));
		entity.setBestAnswer(!entity.isBestAnswer());
		return toDto(commentRepository.save(entity));
	}

	@Transactional
	public LessonCommentDto toggleHidden(UUID commentId) {
		LessonCommentEntity entity = commentRepository.findById(commentId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Comment not found"));
		entity.setHidden(!entity.isHidden());
		return toDto(commentRepository.save(entity));
	}

	private LessonCommentDto toDto(LessonCommentEntity e) {
		return new LessonCommentDto(e.getId(), e.getLessonId(), e.getParentId(), e.getAuthorId(), e.getContent(),
				e.isBestAnswer(), e.isHidden(), e.getCreatedAt());
	}

	public record CreateCommentRequest(UUID parentId, String content) {
	}

	public record LessonCommentDto(UUID id, UUID lessonId, UUID parentId, UUID authorId, String content,
			boolean bestAnswer, boolean hidden, Instant createdAt) {
	}
}
