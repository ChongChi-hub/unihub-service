package com.unihub.academic.config;

import com.unihub.academic.domain.AdminClassEntity;
import com.unihub.academic.domain.FacultyEntity;
import com.unihub.academic.domain.MajorEntity;
import com.unihub.academic.domain.SemesterEntity;
import com.unihub.academic.domain.SubjectEntity;
import com.unihub.academic.repository.AdminClassRepository;
import com.unihub.academic.repository.FacultyRepository;
import com.unihub.academic.repository.MajorRepository;
import com.unihub.academic.repository.SemesterRepository;
import com.unihub.academic.repository.SubjectRepository;
import java.time.LocalDate;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotent academic catalog seeder for local development and demo
 * ([ACD-005]).
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AcademicDataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(AcademicDataSeeder.class);

	private final FacultyRepository facultyRepository;
	private final MajorRepository majorRepository;
	private final AdminClassRepository adminClassRepository;
	private final SemesterRepository semesterRepository;
	private final SubjectRepository subjectRepository;

	public AcademicDataSeeder(FacultyRepository facultyRepository, MajorRepository majorRepository,
			AdminClassRepository adminClassRepository, SemesterRepository semesterRepository,
			SubjectRepository subjectRepository) {
		this.facultyRepository = facultyRepository;
		this.majorRepository = majorRepository;
		this.adminClassRepository = adminClassRepository;
		this.semesterRepository = semesterRepository;
		this.subjectRepository = subjectRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		FacultyEntity fit = ensureFaculty("FIT", "Khoa Công nghệ thông tin");
		FacultyEntity fba = ensureFaculty("FBA", "Khoa Quản trị kinh doanh");

		MajorEntity se = ensureMajor(fit, "SE", "Kỹ thuật phần mềm");
		ensureMajor(fit, "AI", "Trí tuệ nhân tạo");
		ensureMajor(fba, "BA", "Quản trị kinh doanh");

		ensureAdminClass(se, "SE2026A", "Lớp Kỹ thuật phần mềm K26A", 2026);
		ensureSemester("2026-1", "Học kỳ 1 năm học 2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 15),
				true);

		SubjectEntity intro = ensureSubject(fit, "CS101", "Nhập môn Lập trình", 3, Set.of());
		ensureSubject(fit, "CS201", "Cấu trúc dữ liệu & Giải thuật", 4, Set.of(intro));
		ensureSubject(fba, "BA101", "Kinh tế học đại cương", 3, Set.of());

		log.info("Academic seed data verified (idempotent)");
	}

	private FacultyEntity ensureFaculty(String code, String name) {
		return facultyRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
			FacultyEntity f = new FacultyEntity();
			f.setCode(code);
			f.setName(name);
			return facultyRepository.save(f);
		});
	}

	private MajorEntity ensureMajor(FacultyEntity faculty, String code, String name) {
		return majorRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
			MajorEntity m = new MajorEntity();
			m.setFaculty(faculty);
			m.setCode(code);
			m.setName(name);
			return majorRepository.save(m);
		});
	}

	private void ensureAdminClass(MajorEntity major, String code, String name, int cohortYear) {
		if (adminClassRepository.existsByCodeIgnoreCase(code)) {
			return;
		}
		AdminClassEntity ac = new AdminClassEntity();
		ac.setMajor(major);
		ac.setCode(code);
		ac.setName(name);
		ac.setCohortYear(cohortYear);
		adminClassRepository.save(ac);
	}

	private void ensureSemester(String code, String name, LocalDate start, LocalDate end, boolean current) {
		if (semesterRepository.existsByCodeIgnoreCase(code)) {
			return;
		}
		SemesterEntity s = new SemesterEntity();
		s.setCode(code);
		s.setName(name);
		s.setStartDate(start);
		s.setEndDate(end);
		s.setCurrent(current);
		semesterRepository.save(s);
	}

	private SubjectEntity ensureSubject(FacultyEntity faculty, String code, String name, int credits,
			Set<SubjectEntity> prereqs) {
		return subjectRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
			SubjectEntity sub = new SubjectEntity();
			sub.setFaculty(faculty);
			sub.setCode(code);
			sub.setName(name);
			sub.setCredits(credits);
			sub.setPrerequisites(prereqs);
			return subjectRepository.save(sub);
		});
	}
}
