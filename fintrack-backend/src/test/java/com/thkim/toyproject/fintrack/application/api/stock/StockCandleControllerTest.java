package com.thkim.toyproject.fintrack.application.api.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stock-candle-controller-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class StockCandleControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser(roles = "USER")
    void collect_then_analyze_stored_candles() throws Exception {
        mvc.perform(post("/api/stocks/000660/candles/collect")
                        .param("to", "2026-01-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockCode").value("000660"))
                .andExpect(jsonPath("$.stockName").value("Mock Stock"))
                .andExpect(jsonPath("$.collectedCount").value(21))
                .andExpect(jsonPath("$.totalCount").value(21));

        mvc.perform(get("/api/stocks/000660/signals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signal").value("BUY"))
                .andExpect(jsonPath("$.reasons[0]").exists());
    }

    @Test
    void collect_without_authentication_returns_401() throws Exception {
        mvc.perform(post("/api/stocks/000660/candles/collect"))
                .andExpect(status().isUnauthorized());
    }
}
