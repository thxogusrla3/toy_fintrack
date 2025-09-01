package com.thkim.toyproject.fintrack;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class FintrackApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	void login_then_access_protected() throws Exception {
		MvcResult loginResult = mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\": \"thkim\",\"password\": \"1\"}"))
				.andExpect(status().isOk()) //200인지 체크, 401 이면 오류
				.andReturn();

		System.out.println(loginResult.getResponse().getContentAsString());

		// 2. 응답 본문에서 액세스 토큰 추출
		String access = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

		// 3. 응답 헤더에서 'Set-Cookie' 헤더 추출
		String setCookieHeader = loginResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);

		// 4. 'Set-Cookie' 헤더가 존재하고 'refreshToken'을 포함하는지 검증
		Assertions.assertNotNull(setCookieHeader, "Set-Cookie 헤더가 존재해야 합니다.");
		Assertions.assertTrue(setCookieHeader.contains("REFRESH_TOKEN"), "Set-Cookie 헤더에 REFRESH_TOKEN이 포함되어야 합니다.");

		mvc.perform(get("/api/users/me")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
					.header(HttpHeaders.COOKIE, setCookieHeader))
				.andExpect(status().isOk());
	}

}
