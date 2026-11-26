package com.unihub.identity.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unihub.identity.controller.AuthController;
import com.unihub.identity.service.AuthService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = {AuthController.class, RbacMatrixTest.TestRbacController.class})
@Import({SecurityConfig.class, GatewayHeaderAuthenticationFilter.class, SecurityExceptionHandler.class,
		RbacMatrixTest.TestRbacController.class})
class RbacMatrixTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtService jwtService;

	@RestController
	@RequestMapping("/api/v1/test-rbac")
	static class TestRbacController {

		@GetMapping("/admin")
		@PreAuthorize("hasRole('ADMIN')")
		public String adminOnly() {
			return "admin-ok";
		}

		@GetMapping("/lecturer")
		@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
		public String lecturerOrAdmin() {
			return "lecturer-ok";
		}

		@GetMapping("/student")
		@PreAuthorize("hasRole('STUDENT')")
		public String studentOnly() {
			return "student-ok";
		}
	}

	@Test
	void unauthenticatedRequestReturns401ErrorResponse() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void adminCanAccessAdminAndLecturerButNotStudentOnly() throws Exception {
		String userId = UUID.randomUUID().toString();

		mockMvc.perform(get("/api/v1/test-rbac/admin").header("X-User-Id", userId)
				.header("X-User-Email", "admin@unihub.edu.vn").header("X-User-Roles", "ADMIN"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/test-rbac/lecturer").header("X-User-Id", userId)
				.header("X-User-Email", "admin@unihub.edu.vn").header("X-User-Roles", "ADMIN"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/test-rbac/student").header("X-User-Id", userId)
				.header("X-User-Email", "admin@unihub.edu.vn").header("X-User-Roles", "ADMIN"))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void studentCannotAccessAdminOrLecturerEndpoints() throws Exception {
		String userId = UUID.randomUUID().toString();

		mockMvc.perform(get("/api/v1/test-rbac/student").header("X-User-Id", userId)
				.header("X-User-Email", "sv@unihub.edu.vn").header("X-User-Roles", "STUDENT"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/test-rbac/lecturer").header("X-User-Id", userId)
				.header("X-User-Email", "sv@unihub.edu.vn").header("X-User-Roles", "STUDENT"))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));

		mockMvc.perform(get("/api/v1/test-rbac/admin").header("X-User-Id", userId)
				.header("X-User-Email", "sv@unihub.edu.vn").header("X-User-Roles", "STUDENT"))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}
}
