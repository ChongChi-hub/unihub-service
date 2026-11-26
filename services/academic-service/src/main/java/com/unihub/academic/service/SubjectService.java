package com.unihub.academic.service;

import com.unihub.academic.domain.FacultyEntity;
import com.unihub.academic.domain.SubjectEntity;
import com.unihub.academic.dto.AcademicDtos.SubjectDto;
import com.unihub.academic.dto.AcademicDtos.SubjectRequest;
import com.unihub.academic.repository.FacultyRepository;
import com.unihub.academic.repository.SubjectRepository;
import com.unihub.common.api.PageResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SubjectService {

	private final SubjectRepository subjectRepository;
	private final FacultyRepository facultyRepository;

	public SubjectService(SubjectRepository subjectRepository, FacultyRepository facultyRepository) {
		this.subjectRepository = subjectRepository;
		this.facultyRepository = facultyRepository;
	}

	@Transactional(readOnly = true)
	public PageResponse<SubjectDto> list(String query, UUID facultyId, Pageable pageable) {
		Specification<SubjectEntity> spec = (root, cq, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (facultyId != null) {
				predicates.add(cb.equal(root.get("faculty").get("id"), facultyId));
			}
			if (StringUtils.hasText(query)) {
				String pattern = "%" + query.trim().toLowerCase() + "%";
				predicates.add(cb.or(cb.like(cb.lower(root.get("code")), pattern),
						cb.like(cb.lower(root.get("name")), pattern)));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		return PageResponse.from(subjectRepository.findAll(spec, pageable).map(SubjectService::toDto));
	}

	@Transactional
	public SubjectDto create(SubjectRequest request) {
		String code = request.code().trim().toUpperCase();
		if (subjectRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Subject code already exists");
		}
		FacultyEntity faculty = facultyRepository.findById(request.facultyId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Faculty not found"));

		SubjectEntity entity = new SubjectEntity();
		entity.setFaculty(faculty);
		entity.setCode(code);
		entity.setName(request.name().trim());
		entity.setCredits(request.credits());
		entity.setDescription(request.description());
		entity.setPrerequisites(resolvePrerequisites(null, request.prerequisiteIds()));
		return toDto(subjectRepository.save(entity));
	}

	@Transactional
	public SubjectDto update(UUID id, SubjectRequest request) {
		SubjectEntity entity = subjectRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Subject not found"));

		String code = request.code().trim().toUpperCase();
		if (!code.equalsIgnoreCase(entity.getCode()) && subjectRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Subject code already exists");
		}
		FacultyEntity faculty = facultyRepository.findById(request.facultyId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Faculty not found"));

		entity.setFaculty(faculty);
		entity.setCode(code);
		entity.setName(request.name().trim());
		entity.setCredits(request.credits());
		entity.setDescription(request.description());
		entity.setPrerequisites(resolvePrerequisites(id, request.prerequisiteIds()));
		return toDto(subjectRepository.save(entity));
	}

	private Set<SubjectEntity> resolvePrerequisites(UUID selfId, Set<UUID> prerequisiteIds) {
		if (prerequisiteIds == null || prerequisiteIds.isEmpty()) {
			return new HashSet<>();
		}
		Set<SubjectEntity> resolved = new HashSet<>();
		for (UUID prereqId : prerequisiteIds) {
			if (selfId != null && selfId.equals(prereqId)) {
				throw new AppException(ErrorCode.BAD_REQUEST, "Subject cannot be a prerequisite of itself");
			}
			SubjectEntity prereq = subjectRepository.findById(prereqId).orElseThrow(
					() -> new AppException(ErrorCode.NOT_FOUND, "Prerequisite subject not found: " + prereqId));
			if (selfId != null && createsCycle(selfId, prereq)) {
				throw new AppException(ErrorCode.BAD_REQUEST,
						"Cyclic prerequisite detected with subject: " + prereq.getCode());
			}
			resolved.add(prereq);
		}
		return resolved;
	}

	/** Returns true if targetSubject transitively depends on selfId. */
	private static boolean createsCycle(UUID selfId, SubjectEntity candidatePrereq) {
		Set<UUID> visited = new HashSet<>();
		Deque<SubjectEntity> queue = new ArrayDeque<>();
		queue.add(candidatePrereq);

		while (!queue.isEmpty()) {
			SubjectEntity current = queue.poll();
			if (!visited.add(current.getId())) {
				continue;
			}
			if (selfId.equals(current.getId())) {
				return true;
			}
			if (current.getPrerequisites() != null) {
				queue.addAll(current.getPrerequisites());
			}
		}
		return false;
	}

	private static SubjectDto toDto(SubjectEntity e) {
		List<UUID> prereqIds = e.getPrerequisites().stream().map(SubjectEntity::getId).sorted().toList();
		return new SubjectDto(e.getId(), e.getFaculty().getId(), e.getFaculty().getName(), e.getCode(), e.getName(),
				e.getCredits(), e.getDescription(), prereqIds, e.getCreatedAt());
	}
}
