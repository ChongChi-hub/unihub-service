package com.unihub.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.domain.UserStatus;
import com.unihub.identity.dto.CreateUserRequest;
import com.unihub.identity.dto.UserDto;
import com.unihub.identity.repository.RefreshTokenRepository;
import com.unihub.identity.repository.RoleRepository;
import com.unihub.identity.repository.UserRepository;
import com.unihub.identity.security.GatewayUserPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminUserServiceTest {

	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private RefreshTokenRepository refreshTokenRepository;
	private AuditLogService auditLogService;
	private EmailService emailService;
	private PasswordEncoder passwordEncoder;
	private AdminUserService adminUserService;
	private GatewayUserPrincipal adminActor;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		roleRepository = mock(RoleRepository.class);
		refreshTokenRepository = mock(RefreshTokenRepository.class);
		auditLogService = mock(AuditLogService.class);
		emailService = mock(EmailService.class);
		passwordEncoder = new BCryptPasswordEncoder();
		adminUserService = new AdminUserService(userRepository, roleRepository, refreshTokenRepository, passwordEncoder,
				auditLogService, emailService);
		adminActor = new GatewayUserPrincipal(UUID.randomUUID(), "admin@unihub.edu.vn", List.of("ADMIN"));
	}

	@Test
	void createUserPersistsAndRecordsAuditLog() {
		RoleEntity lecturer = new RoleEntity();
		lecturer.setName("LECTURER");

		when(userRepository.existsByEmailIgnoreCase("gv@unihub.edu.vn")).thenReturn(false);
		when(userRepository.existsByCodeIgnoreCase("GV-001")).thenReturn(false);
		when(roleRepository.findByName("LECTURER")).thenReturn(Optional.of(lecturer));
		when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
			UserEntity u = inv.getArgument(0);
			u.setId(UUID.randomUUID());
			return u;
		});

		UserDto created = adminUserService.create(adminActor, new CreateUserRequest("gv@unihub.edu.vn", "Secret@123",
				"Tran Van B", "GV-001", null, null, Set.of("LECTURER")));

		assertThat(created.email()).isEqualTo("gv@unihub.edu.vn");
		assertThat(created.roles()).containsExactly("LECTURER");
		verify(auditLogService).record(eq(adminActor), eq("USER_CREATED"), eq("USER"), eq(created.id().toString()),
				eq(null), eq(created));
	}

	@Test
	void createUserRejectsDuplicateEmail() {
		when(userRepository.existsByEmailIgnoreCase("dup@unihub.edu.vn")).thenReturn(true);

		assertThatThrownBy(() -> adminUserService.create(adminActor,
				new CreateUserRequest("dup@unihub.edu.vn", "Secret@123", "Dup User", null, null, null,
						Set.of("STUDENT"))))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.CONFLICT);
	}

	@Test
	void softDeleteMarksDeletedRevokesTokensAndAudits() {
		UUID targetId = UUID.randomUUID();
		UserEntity target = new UserEntity();
		target.setId(targetId);
		target.setEmail("sv@unihub.edu.vn");
		target.setFullName("Student");
		target.setStatus(UserStatus.ACTIVE);

		when(userRepository.findById(targetId)).thenReturn(Optional.of(target));

		adminUserService.softDelete(adminActor, targetId);

		assertThat(target.getStatus()).isEqualTo(UserStatus.DELETED);
		assertThat(target.getDeletedAt()).isNotNull();
		verify(refreshTokenRepository).revokeAllActiveByUserId(targetId);
		verify(auditLogService).record(eq(adminActor), eq("USER_DELETED"), eq("USER"), eq(targetId.toString()), any(),
				any());
	}

	@Test
	void lockAndUnlockUpdateStatusAndRevokeTokens() {
		UUID targetId = UUID.randomUUID();
		UserEntity target = new UserEntity();
		target.setId(targetId);
		target.setEmail("sv@unihub.edu.vn");
		target.setFullName("Student");
		target.setStatus(UserStatus.ACTIVE);

		when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
		when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		UserDto locked = adminUserService.lock(adminActor, targetId);
		assertThat(locked.status()).isEqualTo(UserStatus.LOCKED);
		verify(refreshTokenRepository).revokeAllActiveByUserId(targetId);

		UserDto unlocked = adminUserService.unlock(adminActor, targetId);
		assertThat(unlocked.status()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	void adminResetPasswordGeneratesTempPasswordAndSendsEmail() {
		UUID targetId = UUID.randomUUID();
		UserEntity target = new UserEntity();
		target.setId(targetId);
		target.setEmail("sv@unihub.edu.vn");
		target.setFullName("Student");
		target.setPasswordHash("old-hash");
		target.setStatus(UserStatus.ACTIVE);

		when(userRepository.findById(targetId)).thenReturn(Optional.of(target));

		String tempPass = adminUserService.resetPasswordByAdmin(adminActor, targetId);

		assertThat(tempPass).hasSize(12);
		assertThat(passwordEncoder.matches(tempPass, target.getPasswordHash())).isTrue();
		verify(refreshTokenRepository).revokeAllActiveByUserId(targetId);
		verify(emailService).sendAdminResetPasswordEmail("sv@unihub.edu.vn", "Student", tempPass);
	}
}
