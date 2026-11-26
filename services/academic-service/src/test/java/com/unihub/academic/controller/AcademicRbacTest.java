package com.unihub.academic.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unihub.academic.security.GatewayHeaderAuthenticationFilter;
import com.unihub.academic.security.SecurityConfig;
import com.unihub.academic.security.SecurityExceptionHandler;
import com.unihub.academic.service.AcademicCatalogService;
import com.unihub.academic.service.SemesterService;
import com.unihub.academic.service.SubjectService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AcademicController.class)
@Import({SecurityConfig.class, GatewayHeaderAuthenticationFilter.class, SecurityExceptionHandler.class})
class AcademicRbacTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AcademicCatalogService catalogService;

	@MockitoBean
	private SemesterService semesterService;

	@MockitoBean
	private SubjectService subjectService;

	@Test
	void studentCanReadFacultiesButCannotCreateFaculty() throws Exception {
		String userId = UUID.randomUUID().toString();

		mockMvc.perform(get("/api/v1/academic/faculties").header("X-User-Id", userId)
				.header("X-User-Email", "sv@unihub.edu.vn").header("X-User-Roles", "STUDENT"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/academic/faculties").header("X-User-Id", userId)
				.header("X-User-Email", "sv@unihub.edu.vn").header("X-User-Roles", "STUDENT")
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"FIT\",\"name\":\"IT\"}"))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void adminCanCreateFaculty() throws Exception {
		String userId = UUID.randomUUID().toString();

		mockMvc.perform(post("/api/v1/academic/faculties").header("X-User-Id", userId)
				.header("X-User-Email", "admin@unihub.edu.vn").header("X-User-Roles", "ADMIN")
				.contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"FIT\",\"name\":\"IT\"}"))
				.andExpect(status().isCreated());
	}
}
