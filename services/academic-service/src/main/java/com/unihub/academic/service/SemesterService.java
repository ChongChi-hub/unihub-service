package com.unihub.academic.service;

import com.unihub.academic.domain.SemesterEntity;
import com.unihub.academic.dto.AcademicDtos.SemesterDto;
import com.unihub.academic.dto.AcademicDtos.SemesterRequest;
import com.unihub.academic.repository.SemesterRepository;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemesterService {

	private final SemesterRepository semesterRepository;

	public SemesterService(SemesterRepository semesterRepository) {
		this.semesterRepository = semesterRepository;
	}

	@Transactional(readOnly = true)
	public List<SemesterDto> listAll() {
		return semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")).stream()
				.map(SemesterService::toDto).toList();
	}

	@Transactional
	public SemesterDto create(SemesterRequest request) {
		validateDates(request, null);
		String code = request.code().trim().toUpperCase();
		if (semesterRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Semester code already exists");
		}
		if (request.current()) {
			semesterRepository.clearCurrentFlag();
		}
		SemesterEntity entity = new SemesterEntity();
		entity.setCode(code);
		entity.setName(request.name().trim());
		entity.setStartDate(request.startDate());
		entity.setEndDate(request.endDate());
		entity.setCurrent(request.current());
		return toDto(semesterRepository.save(entity));
	}

	@Transactional
	public SemesterDto update(UUID id, SemesterRequest request) {
		SemesterEntity entity = semesterRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Semester not found"));
		validateDates(request, id);

		String code = request.code().trim().toUpperCase();
		if (!code.equalsIgnoreCase(entity.getCode()) && semesterRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Semester code already exists");
		}
		if (request.current() && !entity.isCurrent()) {
			semesterRepository.clearCurrentFlag();
		}
		entity.setCode(code);
		entity.setName(request.name().trim());
		entity.setStartDate(request.startDate());
		entity.setEndDate(request.endDate());
		entity.setCurrent(request.current());
		return toDto(semesterRepository.save(entity));
	}

	private void validateDates(SemesterRequest request, UUID excludeId) {
		if (!request.endDate().isAfter(request.startDate())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Semester endDate must be after startDate");
		}
		boolean overlaps = semesterRepository.findOverlapping(request.startDate(), request.endDate()).stream()
				.anyMatch(s -> excludeId == null || !s.getId().equals(excludeId));
		if (overlaps) {
			throw new AppException(ErrorCode.CONFLICT, "Semester date range overlaps with an existing semester");
		}
	}

	private static SemesterDto toDto(SemesterEntity e) {
		return new SemesterDto(e.getId(), e.getCode(), e.getName(), e.getStartDate(), e.getEndDate(), e.isCurrent());
	}
}
