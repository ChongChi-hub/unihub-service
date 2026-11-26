package com.unihub.academic.controller;

import com.unihub.academic.dto.AcademicDtos.AdminClassDto;
import com.unihub.academic.dto.AcademicDtos.AdminClassRequest;
import com.unihub.academic.dto.AcademicDtos.AssignStudentsRequest;
import com.unihub.academic.dto.AcademicDtos.FacultyDto;
import com.unihub.academic.dto.AcademicDtos.FacultyRequest;
import com.unihub.academic.dto.AcademicDtos.MajorDto;
import com.unihub.academic.dto.AcademicDtos.MajorRequest;
import com.unihub.academic.dto.AcademicDtos.SemesterDto;
import com.unihub.academic.dto.AcademicDtos.SemesterRequest;
import com.unihub.academic.dto.AcademicDtos.SubjectDto;
import com.unihub.academic.dto.AcademicDtos.SubjectRequest;
import com.unihub.academic.service.AcademicCatalogService;
import com.unihub.academic.service.SemesterService;
import com.unihub.academic.service.SubjectService;
import com.unihub.common.api.ApiResponse;
import com.unihub.common.api.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academic")
public class AcademicController {

	private final AcademicCatalogService catalogService;
	private final SemesterService semesterService;
	private final SubjectService subjectService;

	public AcademicController(AcademicCatalogService catalogService, SemesterService semesterService,
			SubjectService subjectService) {
		this.catalogService = catalogService;
		this.semesterService = semesterService;
		this.subjectService = subjectService;
	}

	// --- Faculties ---

	@GetMapping("/faculties")
	public ApiResponse<PageResponse<FacultyDto>> listFaculties(@RequestParam(required = false) String query,
			@PageableDefault(size = 20) Pageable pageable) {
		return ApiResponse.ok(catalogService.listFaculties(query, pageable));
	}

	@PostMapping("/faculties")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<FacultyDto> createFaculty(@Valid @RequestBody FacultyRequest request) {
		return ApiResponse.ok("Faculty created", catalogService.createFaculty(request));
	}

	@PutMapping("/faculties/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<FacultyDto> updateFaculty(@PathVariable UUID id, @Valid @RequestBody FacultyRequest request) {
		return ApiResponse.ok("Faculty updated", catalogService.updateFaculty(id, request));
	}

	@DeleteMapping("/faculties/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> deleteFaculty(@PathVariable UUID id) {
		catalogService.deleteFaculty(id);
		return ApiResponse.okMessage("Faculty deleted");
	}

	// --- Majors ---

	@GetMapping("/majors")
	public ApiResponse<PageResponse<MajorDto>> listMajors(@RequestParam(required = false) String query,
			@RequestParam(required = false) UUID facultyId, @PageableDefault(size = 20) Pageable pageable) {
		return ApiResponse.ok(catalogService.listMajors(query, facultyId, pageable));
	}

	@PostMapping("/majors")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<MajorDto> createMajor(@Valid @RequestBody MajorRequest request) {
		return ApiResponse.ok("Major created", catalogService.createMajor(request));
	}

	@PutMapping("/majors/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<MajorDto> updateMajor(@PathVariable UUID id, @Valid @RequestBody MajorRequest request) {
		return ApiResponse.ok("Major updated", catalogService.updateMajor(id, request));
	}

	@DeleteMapping("/majors/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> deleteMajor(@PathVariable UUID id) {
		catalogService.deleteMajor(id);
		return ApiResponse.okMessage("Major deleted");
	}

	// --- Admin Classes ---

	@GetMapping("/admin-classes")
	public ApiResponse<PageResponse<AdminClassDto>> listAdminClasses(@RequestParam(required = false) String query,
			@RequestParam(required = false) UUID majorId, @PageableDefault(size = 20) Pageable pageable) {
		return ApiResponse.ok(catalogService.listAdminClasses(query, majorId, pageable));
	}

	@PostMapping("/admin-classes")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminClassDto> createAdminClass(@Valid @RequestBody AdminClassRequest request) {
		return ApiResponse.ok("Admin class created", catalogService.createAdminClass(request));
	}

	@PostMapping("/admin-classes/{id}/students")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminClassDto> assignStudents(@PathVariable UUID id,
			@Valid @RequestBody AssignStudentsRequest request) {
		return ApiResponse.ok("Students assigned", catalogService.assignStudents(id, request));
	}

	// --- Semesters ---

	@GetMapping("/semesters")
	public ApiResponse<List<SemesterDto>> listSemesters() {
		return ApiResponse.ok(semesterService.listAll());
	}

	@PostMapping("/semesters")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SemesterDto> createSemester(@Valid @RequestBody SemesterRequest request) {
		return ApiResponse.ok("Semester created", semesterService.create(request));
	}

	@PutMapping("/semesters/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SemesterDto> updateSemester(@PathVariable UUID id, @Valid @RequestBody SemesterRequest request) {
		return ApiResponse.ok("Semester updated", semesterService.update(id, request));
	}

	// --- Subjects ---

	@GetMapping("/subjects")
	public ApiResponse<PageResponse<SubjectDto>> listSubjects(@RequestParam(required = false) String query,
			@RequestParam(required = false) UUID facultyId, @PageableDefault(size = 20) Pageable pageable) {
		return ApiResponse.ok(subjectService.list(query, facultyId, pageable));
	}

	@PostMapping("/subjects")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SubjectDto> createSubject(@Valid @RequestBody SubjectRequest request) {
		return ApiResponse.ok("Subject created", subjectService.create(request));
	}

	@PutMapping("/subjects/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SubjectDto> updateSubject(@PathVariable UUID id, @Valid @RequestBody SubjectRequest request) {
		return ApiResponse.ok("Subject updated", subjectService.update(id, request));
	}
}
