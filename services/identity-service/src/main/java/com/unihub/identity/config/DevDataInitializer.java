package com.unihub.identity.config;

import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import com.unihub.identity.domain.UserStatus;
import com.unihub.identity.repository.RoleRepository;
import com.unihub.identity.repository.UserRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds a default local Admin account when app.seed.enabled=true. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DevDataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public DevDataInitializer(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		String adminEmail = "admin@unihub.edu.vn";
		if (userRepository.findByEmailIgnoreCase(adminEmail).isPresent()) {
			return;
		}
		RoleEntity adminRole = roleRepository.findByName("ADMIN")
				.orElseThrow(() -> new IllegalStateException("ADMIN role not seeded"));

		UserEntity admin = new UserEntity();
		admin.setEmail(adminEmail);
		admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
		admin.setFullName("Unihub Administrator");
		admin.setCode("ADMIN-001");
		admin.setStatus(UserStatus.ACTIVE);
		admin.setRoles(Set.of(adminRole));
		userRepository.save(admin);

		log.info("Seeded default local admin account: {}", adminEmail);
	}
}
