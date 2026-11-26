package com.unihub.identity.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.unihub.identity.domain.RoleEntity;
import com.unihub.identity.domain.UserEntity;
import io.jsonwebtoken.Claims;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	@Test
	void generatesAndVerifiesRs256AccessTokenWithClaims() {
		JwtProperties properties = new JwtProperties("unihub-identity", 900_000L, 604_800_000L, "", "");
		JwtService jwtService = new JwtService(properties);

		RoleEntity admin = new RoleEntity();
		admin.setName("ADMIN");

		UserEntity user = new UserEntity();
		user.setId(UUID.randomUUID());
		user.setEmail("admin@unihub.edu.vn");
		user.setFullName("Admin User");
		user.setRoles(Set.of(admin));

		String token = jwtService.generateAccessToken(user);
		Claims claims = jwtService.parseAndVerify(token);

		assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
		assertThat(claims.get("email", String.class)).isEqualTo("admin@unihub.edu.vn");
		assertThat(claims.get("roles", java.util.List.class)).containsExactly("ADMIN");
	}
}
