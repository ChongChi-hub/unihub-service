package com.unihub.identity.service;

import com.unihub.common.api.PageResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.domain.UserStatus;
import com.unihub.identity.dto.CreateUserRequest;
import com.unihub.identity.dto.UpdateUserRequest;
import com.unihub.identity.dto.UserDto;
import com.unihub.identity.repository.RefreshTokenRepository;
import com.unihub.identity.repository.RoleRepository;
import com.unihub.identity.repository.UserRepository;
import com.unihub.identity.security.GatewayUserPrincipal;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminUserService {

	private static final String TEMP_PASS_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditLogService auditLogService;
	private final EmailService emailService;

	public AdminUserService(UserRepository userRepository, RoleRepository roleRepository,
			RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
			AuditLogService auditLogService, EmailService emailService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.auditLogService = auditLogService;
		this.emailService = emailService;
	}

	@Transactional(readOnly = true)
	public PageResponse<UserDto> list(String query, String role, UserStatus status, Pageable pageable) {
		Specification<UserEntity> spec = (root, cq, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.notEqual(root.get("status"), UserStatus.DELETED));

			if (StringUtils.hasText(query)) {
				String pattern = "%" + query.trim().toLowerCase() + "%";
				predicates.add(cb.or(cb.like(cb.lower(root.get("email")), pattern),
						cb.like(cb.lower(root.get("fullName")), pattern),
						cb.like(cb.lower(root.get("code")), pattern)));
			}
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			if (StringUtils.hasText(role)) {
				Join<UserEntity, RoleEntity> roleJoin = root.join("roles");
				predicates.add(cb.equal(roleJoin.get("name"), role.trim().toUpperCase()));
				if (cq != null) {
					cq.distinct(true);
				}
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		Page<UserDto> page = userRepository.findAll(spec, pageable).map(UserDto::from);
		return PageResponse.from(page);
	}

	@Transactional(readOnly = true)
	public UserDto getById(UUID id) {
		return UserDto.from(findActiveOrLocked(id));
	}

	@Transactional
	public UserDto create(GatewayUserPrincipal actor, CreateUserRequest request) {
		String normalizedEmail = request.email().trim().toLowerCase();
		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new AppException(ErrorCode.CONFLICT, "Email already exists");
		}
		String normalizedCode = StringUtils.hasText(request.code()) ? request.code().trim() : null;
		if (normalizedCode != null && userRepository.existsByCodeIgnoreCase(normalizedCode)) {
			throw new AppException(ErrorCode.CONFLICT, "User code already exists");
		}

		UserEntity user = new UserEntity();
		user.setEmail(normalizedEmail);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setFullName(request.fullName().trim());
		user.setCode(normalizedCode);
		user.setDepartmentId(request.departmentId());
		user.setAdminClassId(request.adminClassId());
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(resolveRoles(request.roles()));

		UserEntity saved = userRepository.save(user);
		UserDto dto = UserDto.from(saved);
		auditLogService.record(actor, "USER_CREATED", "USER", saved.getId().toString(), null, dto);
		return dto;
	}

	@Transactional
	public UserDto update(GatewayUserPrincipal actor, UUID id, UpdateUserRequest request) {
		UserEntity user = findActiveOrLocked(id);
		UserDto before = UserDto.from(user);

		String normalizedCode = StringUtils.hasText(request.code()) ? request.code().trim() : null;
		if (normalizedCode != null && !normalizedCode.equalsIgnoreCase(user.getCode())
				&& userRepository.existsByCodeIgnoreCase(normalizedCode)) {
			throw new AppException(ErrorCode.CONFLICT, "User code already exists");
		}

		user.setFullName(request.fullName().trim());
		user.setCode(normalizedCode);
		user.setDepartmentId(request.departmentId());
		user.setAdminClassId(request.adminClassId());
		user.setRoles(resolveRoles(request.roles()));

		UserEntity saved = userRepository.save(user);
		UserDto after = UserDto.from(saved);
		auditLogService.record(actor, "USER_UPDATED", "USER", saved.getId().toString(), before, after);
		return after;
	}

	@Transactional
	public void softDelete(GatewayUserPrincipal actor, UUID id) {
		UserEntity user = findActiveOrLocked(id);
		if (actor != null && actor.userId().equals(user.getId())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Cannot delete your own account");
		}
		UserDto before = UserDto.from(user);
		user.setStatus(UserStatus.DELETED);
		user.setDeletedAt(Instant.now());
		userRepository.save(user);
		refreshTokenRepository.revokeAllActiveByUserId(user.getId());

		auditLogService.record(actor, "USER_DELETED", "USER", user.getId().toString(), before, UserDto.from(user));
	}

	@Transactional
	public UserDto lock(GatewayUserPrincipal actor, UUID id) {
		UserEntity user = findActiveOrLocked(id);
		if (actor != null && actor.userId().equals(user.getId())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Cannot lock your own account");
		}
		UserDto before = UserDto.from(user);
		user.setStatus(UserStatus.LOCKED);
		UserEntity saved = userRepository.save(user);
		refreshTokenRepository.revokeAllActiveByUserId(saved.getId());

		UserDto after = UserDto.from(saved);
		auditLogService.record(actor, "USER_LOCKED", "USER", saved.getId().toString(), before, after);
		return after;
	}

	@Transactional
	public UserDto unlock(GatewayUserPrincipal actor, UUID id) {
		UserEntity user = findActiveOrLocked(id);
		UserDto before = UserDto.from(user);
		user.setStatus(UserStatus.ACTIVE);
		UserEntity saved = userRepository.save(user);

		UserDto after = UserDto.from(saved);
		auditLogService.record(actor, "USER_UNLOCKED", "USER", saved.getId().toString(), before, after);
		return after;
	}

	@Transactional
	public String resetPasswordByAdmin(GatewayUserPrincipal actor, UUID id) {
		UserEntity user = findActiveOrLocked(id);
		String tempPassword = generateTemporaryPassword();
		user.setPasswordHash(passwordEncoder.encode(tempPassword));
		userRepository.save(user);
		refreshTokenRepository.revokeAllActiveByUserId(user.getId());

		emailService.sendAdminResetPasswordEmail(user.getEmail(), user.getFullName(), tempPassword);
		auditLogService.record(actor, "USER_PASSWORD_RESET_BY_ADMIN", "USER", user.getId().toString(), null, null);
		return tempPassword;
	}

	private static String generateTemporaryPassword() {
		StringBuilder sb = new StringBuilder(12);
		for (int i = 0; i < 12; i++) {
			sb.append(TEMP_PASS_CHARS.charAt(RANDOM.nextInt(TEMP_PASS_CHARS.length())));
		}
		return sb.toString();
	}

	private UserEntity findActiveOrLocked(UUID id) {
		UserEntity user = userRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
		if (user.getStatus() == UserStatus.DELETED) {
			throw new AppException(ErrorCode.NOT_FOUND, "User not found");
		}
		return user;
	}

	private Set<RoleEntity> resolveRoles(Set<String> roleNames) {
		Set<RoleEntity> roles = new HashSet<>();
		for (String name : roleNames) {
			String normalized = name.trim().toUpperCase();
			RoleEntity role = roleRepository.findByName(normalized)
					.orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Unknown role: " + normalized));
			roles.add(role);
		}
		return roles;
	}
}
