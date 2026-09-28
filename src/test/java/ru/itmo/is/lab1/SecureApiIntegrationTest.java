package ru.itmo.is.lab1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecureApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void loginReturnsJwtAndRejectsWrongPassword() throws Exception {
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"student","password":"Student#Lab1"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.username").value("student"));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"student","password":"wrong-password"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Неверные учетные данные"));
	}

	@Test
	void dataRequiresTokenAndDoesNotLeakPasswordHash() throws Exception {
		mockMvc.perform(get("/api/data"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Требуется аутентификация"));

		String token = login("student", "Student#Lab1");
		mockMvc.perform(get("/api/data").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].title").value("Политика паролей"))
				.andExpect(content().string(not(containsString("passwordHash"))))
				.andExpect(content().string(not(containsString("$2a$"))))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")));
	}

	@Test
	void sqlInjectionPayloadIsTreatedAsLiteralText() throws Exception {
		String token = login("student", "Student#Lab1");
		mockMvc.perform(get("/api/data")
						.param("q", "' OR '1'='1")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/api/data")
						.param("q", "парол")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title").value("Политика паролей"));
	}

	@Test
	void notesEscapeHtmlAndStayPrivateToTheAuthor() throws Exception {
		String student = login("student", "Student#Lab1");
		mockMvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/notes")
						.header("Authorization", "Bearer " + student)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"<script>alert(1)</script>","content":"hello <b>world</b>"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("&lt;script&gt;alert(1)&lt;/script&gt;"))
				.andExpect(jsonPath("$.content").value("hello &lt;b&gt;world&lt;/b&gt;"))
				.andExpect(jsonPath("$.author").value("student"));

		mockMvc.perform(get("/api/notes").header("Authorization", "Bearer " + student))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("&lt;script&gt;alert(1)&lt;/script&gt;"));

		String admin = login("admin", "Admin#Lab1");
		mockMvc.perform(get("/api/notes").header("Authorization", "Bearer " + admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/api/notes").header("Authorization", "Bearer not-a-jwt"))
				.andExpect(status().isUnauthorized());
	}

	private String login(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
	}

}
