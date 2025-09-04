package com.thkim.toyproject.fintrack.security;


import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {
    @Autowired
    MockMvc mvc;

    private final String USERNAME = "thkim";
    private final String PASSWORD   = "1";
    private final String REFRESH_TOKEN   = "REFRESH_TOKEN";

    @Test
    void login_then_access_protected() throws Exception {
        MvcResult loginResult = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + USERNAME + "\", \"password\":\"1\"}"))
                .andExpect(status().isOk()) //200인지 체크, 401 이면 오류
                .andReturn();

        System.out.println(loginResult.getResponse().getContentAsString());

        // 2. 응답 본문에서 액세스 토큰 추출
        String access = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        // 3. 응답 헤더에서 'Set-Cookie' 헤더 추출
        String setCookieHeader = loginResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        // 4. 'Set-Cookie' 헤더가 존재하고 'refreshToken'을 포함하는지 검증
        Assertions.assertNotNull(setCookieHeader, "Set-Cookie 헤더가 존재해야 합니다.");
        Assertions.assertTrue(setCookieHeader.contains(REFRESH_TOKEN), "Set-Cookie 헤더에 " + REFRESH_TOKEN + "이 포함되어야 합니다.");

        mvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                        .header(HttpHeaders.COOKIE, setCookieHeader))
                .andExpect(status().isOk());
    }

    @Test
    void me_withoutToken_401() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void admin_withUserRole_403() throws Exception {
        mvc.perform(get("/api/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_wrongPassword_401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + USERNAME + "\", \"password\":\"WRONG\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_should_return_new_access_token() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + USERNAME + "\", \"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(REFRESH_TOKEN))
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String access1 = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        Cookie refreshCookie = extractCookie(login);
        assertThat(refreshCookie).isNotNull();

        MvcResult refresh1 = mvc.perform(post("/api/auth/refresh")
                .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String access2 = JsonPath.read(refresh1.getResponse().getContentAsString(), "$.accessToken");
        assertThat(access2).isNotBlank();

        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + access2))
                .andExpect(status().isOk());
    }

    private Cookie extractCookie(MvcResult res) {
        return Arrays.stream(res.getResponse().getCookies())
                .filter(c -> "REFRESH_TOKEN".equals(c.getName()))
                .findFirst().orElse(null);
    }


}
