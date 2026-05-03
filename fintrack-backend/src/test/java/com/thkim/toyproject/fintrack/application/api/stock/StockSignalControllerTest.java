package com.thkim.toyproject.fintrack.application.api.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stock-signal-controller-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class StockSignalControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser(roles = "USER")
    void analyze_returns_trade_signal() throws Exception {
        mvc.perform(post("/api/stocks/signals/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buySignalRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signal").value("BUY"))
                .andExpect(jsonPath("$.reasons[0]").exists());
    }

    @Test
    void analyze_without_authentication_returns_401() throws Exception {
        mvc.perform(post("/api/stocks/signals/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buySignalRequestJson()))
                .andExpect(status().isUnauthorized());
    }

    private String buySignalRequestJson() {
        StringBuilder json = new StringBuilder("{\"candles\":[");
        LocalDate baseDate = LocalDate.of(2026, 1, 1);

        for (int i = 0; i < 19; i++) {
            if (i > 0) {
                json.append(",");
            }
            int close = 100 + i;
            json.append(candleJson(baseDate.plusDays(i), close, close + 1, close - 1, close, 1000));
        }

        json.append(",")
                .append(candleJson(baseDate.plusDays(19), 118, 118, 115, 116, 700))
                .append(",")
                .append(candleJson(baseDate.plusDays(20), 116, 121, 115, 120, 2000))
                .append("]}");

        return json.toString();
    }

    private String candleJson(LocalDate date, int open, int high, int low, int close, long volume) {
        return """
                {
                  "date": "%s",
                  "open": %d,
                  "high": %d,
                  "low": %d,
                  "close": %d,
                  "volume": %d
                }
                """.formatted(date, open, high, low, close, volume);
    }
}
