package com.unihub.academic.service;

import com.unihub.academic.domain.AdminClassEntity;
import com.unihub.academic.domain.FacultyEntity;
import com.unihub.academic.domain.MajorEntity;
import com.unihub.academic.dto.AcademicDtos.AdminClassDto;
import com.unihub.academic.dto.AcademicDtos.AdminClassRequest;
import com.unihub.academic.dto.AcademicDtos.AssignStudentsRequest;
import com.unihub.academic.dto.AcademicDtos.FacultyDto;
import com.unihub.academic.dto.AcademicDtos.FacultyRequest;
import com.unihub.academic.dto.AcademicDtos.MajorDto;
import com.unihub.academic.dto.AcademicDtos.MajorRequest;
import com.unihub.academic.repository.AdminClassRepository;
import com.unihub.academic.repository.FacultyRepository;
import com.unihub.academic.repository.MajorRepository;
import com.unihub.academic.repository.SubjectRepository;
import com.unihub.common.api.PageResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AcademicCatalogService {

	private final FacultyRepository facultyRepository;
	private final MajorRepository majorRepository;
	private final AdminClassRepository adminClassRepository;
	private final SubjectRepository subjectRepository;

	public AcademicCatalogService(FacultyRepository facultyRepository, MajorRepository majorRepository,
			AdminClassRepository adminClassRepository, SubjectRepository subjectRepository) {
		this.facultyRepository = facultyRepository;
		this.majorRepository = majorRepository;
		this.adminClassRepository = adminClassRepository;
		this.subjectRepository = subjectRepository;
	}

	// --- Faculties ---

	@Transactional(readOnly = true)
	public PageResponse<FacultyDto> listFaculties(String query, Pageable pageable) {
		Specification<FacultyEntity> spec = (root, cq, cb) -> {
			if (!StringUtils.hasText(query)) {
				return cb.conjunction();
			}
			String pattern = "%" + query.trim().toLowerCase() + "%";
			return cb.or(cb.like(cb.lower(root.get("code")), pattern), cb.like(cb.lower(root.get("name")), pattern));
		};
		return PageResponse.from(facultyRepository.findAll(spec, pageable).map(AcademicCatalogService::toFacultyDto));
	}

	@Transactional
	public FacultyDto createFaculty(FacultyRequest request) {
		String code = request.code().trim().toUpperCase();
		if (facultyRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Faculty code already exists");
		}
		FacultyEntity entity = new FacultyEntity();
		entity.setCode(code);
		entity.setName(request.name().trim());
		return toFacultyDto(facultyRepository.save(entity));
	}

	@Transactional
	public FacultyDto updateFaculty(UUID id, FacultyRequest request) {
		FacultyEntity entity = facultyRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Faculty not found"));
		String code = request.code().trim().toUpperCase();
		if (!code.equalsIgnoreCase(entity.getCode()) && facultyRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Faculty code already exists");
		}
		entity.setCode(code);
		entity.setName(request.name().trim());
		return toFacultyDto(facultyRepository.save(entity));
	}

	@Transactional
	public void deleteFaculty(UUID id) {
		FacultyEntity entity = facultyRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Faculty not found"));
		if (majorRepository.existsByFacultyId(id) || subjectRepository.existsByFacultyId(id)) {
			throw new AppException(ErrorCode.CONFLICT, "Cannot delete faculty referenced by majors or subjects");
		}
		facultyRepository.delete(entity);
	}

	// --- Majors ---

	@Transactional(readOnly = true)
	public PageResponse<MajorDto> listMajors(String query, UUID facultyId, Pageable pageable) {
		Specification<MajorEntity> spec = (root, cq, cb) -> {
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
		return PageResponse.from(majorRepository.findAll(spec, pageable).map(AcademicCatalogService::toMajorDto));
	}

	@Transactional
	public MajorDto createMajor(MajorRequest request) {
		String code = request.code().trim().toUpperCase();
		if (majorRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Major code already exists");
		}
		FacultyEntity faculty = facultyRepository.findById(request.facultyId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Faculty not found"));
		MajorEntity entity = new MajorEntity();
		entity.setFaculty(faculty);
		entity.setCode(code);
		entity.setName(request.name().trim());
		return toMajorDto(majorRepository.save(entity));
	}

	@Transactional
	public MajorDto updateMajor(UUID id, MajorRequest request) {
		MajorEntity entity = majorRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Major not found"));
		String code = request.code().trim().toUpperCase();
		if (!code.equalsIgnoreCase(entity.getCode()) && majorRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Major code already exists");
		}
		FacultyEntity faculty = facultyRepository.findById(request.facultyId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Faculty not found"));
		entity.setFaculty(faculty);
		entity.setCode(code);
		entity.setName(request.name().trim());
		return toMajorDto(majorRepository.save(entity));
	}

	@Transactional
	public void deleteMajor(UUID id) {
		MajorEntity entity = majorRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Major not found"));
		if (adminClassRepository.existsByMajorId(id)) {
			throw new AppException(ErrorCode.CONFLICT, "Cannot delete major referenced by admin classes");
		}
		majorRepository.delete(entity);
	}

	// --- Admin Classes ---

	@Transactional(readOnly = true)
	public PageResponse<AdminClassDto> listAdminClasses(String query, UUID majorId, Pageable pageable) {
		Specification<AdminClassEntity> spec = (root, cq, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (majorId != null) {
				predicates.add(cb.equal(root.get("major").get("id"), majorId));
			}
			if (StringUtils.hasText(query)) {
				String pattern = "%" + query.trim().toLowerCase() + "%";
				predicates.add(cb.or(cb.like(cb.lower(root.get("code")), pattern),
						cb.like(cb.lower(root.get("name")), pattern)));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		return PageResponse
				.from(adminClassRepository.findAll(spec, pageable).map(AcademicCatalogService::toAdminClassDto));
	}

	@Transactional
	public AdminClassDto createAdminClass(AdminClassRequest request) {
		String code = request.code().trim().toUpperCase();
		if (adminClassRepository.existsByCodeIgnoreCase(code)) {
			throw new AppException(ErrorCode.CONFLICT, "Admin class code already exists");
		}
		MajorEntity major = majorRepository.findById(request.majorId())
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Major not found"));
		AdminClassEntity entity = new AdminClassEntity();
		entity.setMajor(major);
		entity.setCode(code);
		entity.setName(request.name().trim());
		entity.setCohortYear(request.cohortYear());
		return toAdminClassDto(adminClassRepository.save(entity));
	}

	@Transactional
	public AdminClassDto assignStudents(UUID classId, AssignStudentsRequest request) {
		AdminClassEntity entity = adminClassRepository.findById(classId)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Admin class not found"));
		entity.getStudentIds().addAll(request.studentIds());
		return toAdminClassDto(adminClassRepository.save(entity));
	}

	private static FacultyDto toFacultyDto(FacultyEntity e) {
		return new FacultyDto(e.getId(), e.getCode(), e.getName(), e.getCreatedAt());
	}

	private static MajorDto toMajorDto(MajorEntity e) {
		return new MajorDto(e.getId(), e.getFaculty().getId(), e.getFaculty().getName(), e.getCode(), e.getName(),
				e.getCreatedAt());
	}

	private static AdminClassDto toAdminClassDto(AdminClassEntity e) {
		return new AdminClassDto(e.getId(), e.getMajor().getId(), e.getMajor().getName(), e.getCode(), e.getName(),
				e.getCohortYear(), e.getStudentIds().size(), e.getCreatedAt());
	}
}
