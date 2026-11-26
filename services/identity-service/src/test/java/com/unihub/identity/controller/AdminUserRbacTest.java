package com.unihub.identity.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unihub.identity.security.GatewayHeaderAuthenticationFilter;
import com.unihub.identity.security.JwtService;
import com.unihub.identity.security.SecurityConfig;
import com.unihub.identity.security.SecurityExceptionHandler;
import com.unihub.identity.service.AdminUserService;
import com.unihub.identity.service.AuditLogService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AdminUserController.class, AuditLogController.class})
@Import({SecurityConfig.class, GatewayHeaderAuthenticationFilter.class, SecurityExceptionHandler.class})
class AdminUserRbacTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdminUserService adminUserService;

	@MockitoBean
	private AuditLogService auditLogService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void lecturerCannotAccessUsersOrAuditLogs() throws Exception {
		String userId = UUID.randomUUID().toString();

		mockMvc.perform(get("/api/v1/users").header("X-User-Id", userId).header("X-User-Email", "gv@unihub.edu.vn")
				.header("X-User-Roles", "LECTURER")).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));

		mockMvc.perform(get("/api/v1/audit-logs").header("X-User-Id", userId).header("X-User-Email", "gv@unihub.edu.vn")
				.header("X-User-Roles", "LECTURER")).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void adminCanAccessUsersAndAuditLogs() throws Exception {
		String userId = UUID.randomUUID().toString();

		mockMvc.perform(get("/api/v1/users").header("X-User-Id", userId).header("X-User-Email", "admin@unihub.edu.vn")
				.header("X-User-Roles", "ADMIN")).andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/audit-logs").header("X-User-Id", userId)
				.header("X-User-Email", "admin@unihub.edu.vn").header("X-User-Roles", "ADMIN"))
				.andExpect(status().isOk());
	}
}
