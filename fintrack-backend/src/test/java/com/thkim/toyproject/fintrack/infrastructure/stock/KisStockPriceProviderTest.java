package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisStockPriceProviderTest {
    @Test
    void getDailyCandles_requests_token_and_maps_daily_chart_response() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        KisProperties properties = kisProperties();
        KisStockPriceProvider provider = new KisStockPriceProvider(properties, new org.springframework.boot.web.client.RestTemplateBuilder());
        provider.setRestTemplate(restTemplate);

        server.expect(requestTo("https://openapi.koreainvestment.com:9443/oauth2/tokenP"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.grant_type").value("client_credentials"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "test-access-token",
                          "expires_in": 86400
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=000660&FID_INPUT_DATE_1=20260101&FID_INPUT_DATE_2=20260102&FID_PERIOD_DIV_CODE=D&FID_ORG_ADJ_PRC=0"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("authorization", "Bearer test-access-token"))
                .andExpect(header("appkey", "test-app-key"))
                .andExpect(header("appsecret", "test-app-secret"))
                .andExpect(header("tr_id", "FHKST03010100"))
                .andRespond(withSuccess("""
                        {
                          "rt_cd": "0",
                          "msg1": "정상처리 되었습니다.",
                          "output2": [
                            {
                              "stck_bsop_date": "20260102",
                              "stck_oprc": "101000",
                              "stck_hgpr": "103000",
                              "stck_lwpr": "100000",
                              "stck_clpr": "102000",
                              "acml_vol": "2000000"
                            },
                            {
                              "stck_bsop_date": "20260101",
                              "stck_oprc": "100000",
                              "stck_hgpr": "102000",
                              "stck_lwpr": "99000",
                              "stck_clpr": "101000",
                              "acml_vol": "1500000"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<StockCandle> candles = provider.getDailyCandles(
                "000660",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 2)
        );

        assertThat(candles).hasSize(2);
        assertThat(candles.get(0).date()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(candles.get(0).close()).isEqualByComparingTo("101000");
        assertThat(candles.get(1).volume()).isEqualTo(2_000_000L);
        server.verify();
    }

    private KisProperties kisProperties() {
        KisProperties properties = new KisProperties();
        properties.setBaseUrl("https://openapi.koreainvestment.com:9443");
        properties.setAppKey("test-app-key");
        properties.setAppSecret("test-app-secret");
        properties.setAccountNo("12345678-01");
        return properties;
    }
}
