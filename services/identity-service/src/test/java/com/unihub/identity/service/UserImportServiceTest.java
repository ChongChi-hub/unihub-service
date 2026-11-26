package com.unihub.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.dto.UserImportPreviewResponse;
import com.unihub.identity.dto.UserImportResultResponse;
import com.unihub.identity.repository.RoleRepository;
import com.unihub.identity.repository.UserRepository;
import com.unihub.identity.security.GatewayUserPrincipal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserImportServiceTest {

	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private AuditLogService auditLogService;
	private UserImportService userImportService;
	private GatewayUserPrincipal adminActor;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		roleRepository = mock(RoleRepository.class);
		auditLogService = mock(AuditLogService.class);
		userImportService = new UserImportService(userRepository, roleRepository, new BCryptPasswordEncoder(),
				auditLogService);
		adminActor = new GatewayUserPrincipal(UUID.randomUUID(), "admin@unihub.edu.vn", List.of("ADMIN"));

		RoleEntity student = new RoleEntity();
		student.setName("STUDENT");
		when(roleRepository.findAll()).thenReturn(List.of(student));
	}

	@Test
	void previewReportsRowLevelErrorsWithoutTouchingDb() {
		String csv = "email,fullName,code,roles,password\n" + "sv1@unihub.edu.vn,Nguyen Van A,SV01,STUDENT,Secret@123\n"
				+ "invalid-email,Missing Email,SV02,STUDENT,Secret@123\n"
				+ "sv1@unihub.edu.vn,Duplicate Email,SV03,STUDENT,Secret@123\n";

		MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv",
				csv.getBytes(StandardCharsets.UTF_8));

		UserImportPreviewResponse preview = userImportService.preview(file);

		assertThat(preview.totalRows()).isEqualTo(3);
		assertThat(preview.validCount()).isEqualTo(1);
		assertThat(preview.errorCount()).isEqualTo(2);
		assertThat(preview.errors()).hasSize(2);
	}

	@Test
	void importUsersCommitsAllValidRowsAndAbortsIfAnyRowHasError() {
		String validCsv = "email,fullName,code,roles,password\n"
				+ "sv1@unihub.edu.vn,Nguyen Van A,SV01,STUDENT,Secret@123\n"
				+ "sv2@unihub.edu.vn,Tran Van B,SV02,STUDENT,Secret@123\n";
		MockMultipartFile validFile = new MockMultipartFile("file", "users.csv", "text/csv",
				validCsv.getBytes(StandardCharsets.UTF_8));

		UserImportResultResponse okResult = userImportService.importUsers(adminActor, validFile);
		assertThat(okResult.importedCount()).isEqualTo(2);
		assertThat(okResult.failedCount()).isZero();
		verify(userRepository).saveAll(anyList());

		String invalidCsv = "email,fullName,code,roles,password\n"
				+ "sv3@unihub.edu.vn,Nguyen Van C,SV03,STUDENT,short\n";
		MockMultipartFile invalidFile = new MockMultipartFile("file", "bad.csv", "text/csv",
				invalidCsv.getBytes(StandardCharsets.UTF_8));

		UserImportResultResponse badResult = userImportService.importUsers(adminActor, invalidFile);
		assertThat(badResult.importedCount()).isZero();
		assertThat(badResult.failedCount()).isEqualTo(1);
	}

	@Test
	void generatesValidExcelTemplateAndParsesIt() {
		byte[] xlsx = userImportService.generateExcelTemplate();
		assertThat(xlsx.length).isGreaterThan(100);

		MockMultipartFile excelFile = new MockMultipartFile("file", "template.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsx);
		UserImportPreviewResponse preview = userImportService.preview(excelFile);

		assertThat(preview.totalRows()).isEqualTo(1);
		assertThat(preview.validCount()).isEqualTo(1);
		verify(userRepository, never()).saveAll(anyList());
	}
}
