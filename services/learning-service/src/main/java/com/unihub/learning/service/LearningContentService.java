package com.unihub.learning.service;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.learning.domain.ChapterEntity;
import com.unihub.learning.domain.LessonAttachmentEntity;
import com.unihub.learning.domain.LessonEntity;
import com.unihub.learning.domain.LessonProgressEntity;
import com.unihub.learning.dto.LearningDtos.AttachmentDto;
import com.unihub.learning.dto.LearningDtos.AttachmentRequest;
import com.unihub.learning.dto.LearningDtos.ChapterDto;
import com.unihub.learning.dto.LearningDtos.ChapterRequest;
import com.unihub.learning.dto.LearningDtos.LessonDto;
import com.unihub.learning.dto.LearningDtos.LessonRequest;
import com.unihub.learning.dto.LearningDtos.ProgressSummaryDto;
import com.unihub.learning.dto.LearningDtos.ReorderRequest;
import com.unihub.learning.repository.ChapterRepository;
import com.unihub.learning.repository.LessonAttachmentRepository;
import com.unihub.learning.repository.LessonProgressRepository;
import com.unihub.learning.repository.LessonRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningContentService {

	private final ChapterRepository chapterRepository;
	private final LessonRepository lessonRepository;
	private final LessonAttachmentRepository attachmentRepository;
	private final LessonProgressRepository progressRepository;

	public LearningContentService(ChapterRepository chapterRepository, LessonRepository lessonRepository,
			LessonAttachmentRepository attachmentRepository, LessonProgressRepository progressRepository) {
		this.chapterRepository = chapterRepository;
		this.lessonRepository = lessonRepository;
		this.attachmentRepository = attachmentRepository;
		this.progressRepository = progressRepository;
	}

	@Transactional(readOnly = true)
	public List<ChapterDto> getSectionOutline(UUID courseSectionId, boolean includeDrafts) {
		return chapterRepository.findByCourseSectionIdOrderBySortOrderAsc(courseSectionId).stream()
				.filter(ch -> includeDrafts || ch.isPublished()).map(ch -> {
					List<LessonDto> lessons = lessonRepository.findByChapterIdOrderBySortOrderAsc(ch.getId()).stream()
							.filter(l -> includeDrafts || l.isPublished()).map(this::toLessonDto).toList();
					return new ChapterDto(ch.getId(), ch.getCourseSectionId(), ch.getTitle(), ch.getSortOrder(),
							ch.isPublished(), lessons);
				}).toList();
	}

	@Transactional
	public ChapterDto createChapter(ChapterRequest request) {
		ChapterEntity chapter = new ChapterEntity();
		chapter.setCourseSectionId(request.courseSectionId());
		chapter.setTitle(request.title().trim());
		chapter.setSortOrder(request.sortOrder());
		chapter.setPublished(request.published());
		ChapterEntity saved = chapterRepository.save(chapter);
		return new ChapterDto(saved.getId(), saved.getCourseSectionId(), saved.getTitle(), saved.getSortOrder(),
				saved.isPublished(), List.of());
	}

	@Transactional
	public void reorderChapters(ReorderRequest request) {
		List<UUID> ids = request.orderedIds();
		for (int i = 0; i < ids.size(); i++) {
			ChapterEntity ch = chapterRepository.findById(ids.get(i))
					.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chapter not found"));
			ch.setSortOrder(i);
			chapterRepository.save(ch);
		}
	}

	@Transactional
	public LessonDto createLesson(LessonRequest request) {
		ChapterEntity chapter = chapterRepository.findById(request.chapterId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chapter not found"));
		LessonEntity lesson = new LessonEntity();
		lesson.setChapter(chapter);
		lesson.setTitle(request.title().trim());
		lesson.setContent(request.content());
		lesson.setVideoUrl(request.videoUrl());
		lesson.setVideoFileId(request.videoFileId());
		lesson.setSortOrder(request.sortOrder());
		lesson.setPublished(request.published());
		return toLessonDto(lessonRepository.save(lesson));
	}

	@Transactional
	public AttachmentDto addAttachment(UUID lessonId, AttachmentRequest request) {
		LessonEntity lesson = lessonRepository.findById(lessonId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lesson not found"));
		LessonAttachmentEntity att = new LessonAttachmentEntity();
		att.setLesson(lesson);
		att.setFileId(request.fileId());
		att.setFileName(request.fileName().trim());
		att.setContentType(request.contentType());
		att.setFileSize(request.fileSize());
		LessonAttachmentEntity saved = attachmentRepository.save(att);
		return new AttachmentDto(saved.getId(), saved.getFileId(), saved.getFileName(), saved.getContentType(),
				saved.getFileSize());
	}

	@Transactional
	public void removeAttachment(UUID attachmentId) {
		if (!attachmentRepository.existsById(attachmentId)) {
			throw new AppException(ErrorCode.NOT_FOUND, "Attachment not found");
		}
		attachmentRepository.deleteById(attachmentId);
	}

	@Transactional
	public ProgressSummaryDto markLessonCompleted(UUID studentId, UUID lessonId) {
		LessonEntity lesson = lessonRepository.findById(lessonId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Lesson not found"));

		LessonProgressEntity.ProgressKey key = new LessonProgressEntity.ProgressKey(lessonId, studentId);
		if (!progressRepository.existsById(key)) {
			LessonProgressEntity progress = new LessonProgressEntity();
			progress.setLessonId(lessonId);
			progress.setStudentId(studentId);
			progressRepository.save(progress);
		}
		return getSectionProgress(studentId, lesson.getChapter().getCourseSectionId());
	}

	@Transactional(readOnly = true)
	public ProgressSummaryDto getSectionProgress(UUID studentId, UUID courseSectionId) {
		List<LessonEntity> published = lessonRepository.findPublishedByCourseSection(courseSectionId);
		if (published.isEmpty()) {
			return new ProgressSummaryDto(courseSectionId, 0, 0, 0);
		}
		List<UUID> lessonIds = published.stream().map(LessonEntity::getId).toList();
		Set<UUID> completedIds = progressRepository.findByStudentIdAndLessonIdIn(studentId, lessonIds).stream()
				.map(LessonProgressEntity::getLessonId).collect(Collectors.toSet());

		int completed = completedIds.size();
		int percent = (int) Math.round((completed * 100.0) / published.size());
		return new ProgressSummaryDto(courseSectionId, published.size(), completed, percent);
	}

	private LessonDto toLessonDto(LessonEntity l) {
		List<AttachmentDto> attachments = attachmentRepository.findByLessonId(l.getId()).stream().map(
				a -> new AttachmentDto(a.getId(), a.getFileId(), a.getFileName(), a.getContentType(), a.getFileSize()))
				.toList();
		return new LessonDto(l.getId(), l.getChapter().getId(), l.getTitle(), l.getContent(), l.getVideoUrl(),
				l.getVideoFileId(), l.getSortOrder(), l.isPublished(), attachments);
	}
}
