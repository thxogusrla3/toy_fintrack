package com.thkim.toyproject.fintrack.application.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:frontend-controller-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class FrontendControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void stock_theme_signal_route_forwards_to_react_app() throws Exception {
        mvc.perform(get("/stocks/theme-signals"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }
}
