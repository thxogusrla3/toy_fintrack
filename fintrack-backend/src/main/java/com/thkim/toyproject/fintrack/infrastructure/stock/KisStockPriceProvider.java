package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.StockPriceProvider;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleSeries;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnProperty(name = "app.stock.provider", havingValue = "kis")
public class KisStockPriceProvider implements StockPriceProvider {
    private static final DateTimeFormatter KIS_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String DAILY_CHART_PATH = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
    private static final String DAILY_CHART_TR_ID = "FHKST03010100";

    private final KisProperties kisProperties;
    private RestTemplate restTemplate;
    private String cachedAccessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public KisStockPriceProvider(KisProperties kisProperties, RestTemplateBuilder restTemplateBuilder) {
        this.kisProperties = kisProperties;
        this.restTemplate = restTemplateBuilder.build();
    }

    void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public StockCandleSeries getDailyCandleSeries(String stockCode, LocalDate from, LocalDate to) {
        validateProperties();

        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDate startDate = from == null ? endDate.minusDays(60) : from;

        String url = UriComponentsBuilder.fromHttpUrl(kisProperties.getBaseUrl())
                .path(DAILY_CHART_PATH)
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", stockCode)
                .queryParam("FID_INPUT_DATE_1", startDate.format(KIS_DATE_FORMAT))
                .queryParam("FID_INPUT_DATE_2", endDate.format(KIS_DATE_FORMAT))
                .queryParam("FID_PERIOD_DIV_CODE", "D")
                .queryParam("FID_ORG_ADJ_PRC", "0")
                .toUriString();

        HttpEntity<Void> request = new HttpEntity<>(kisHeaders(DAILY_CHART_TR_ID));
        ResponseEntity<KisDailyChartResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                KisDailyChartResponse.class
        );

        KisDailyChartResponse body = Objects.requireNonNull(response.getBody(), "KIS daily chart response body is null");
        if (!"0".equals(body.rtCd())) {
            throw new IllegalStateException("KIS daily chart request failed: " + body.msg1());
        }

        List<StockCandle> candles = body.output2().stream()
                .map(KisDailyCandle::toStockCandle)
                .sorted((left, right) -> left.date().compareTo(right.date()))
                .toList();
        String stockName = body.output1() == null ? null : body.output1().stockName();
        return new StockCandleSeries(stockName, candles);
    }

    private synchronized String accessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
            return cachedAccessToken;
        }

        validateProperties();

        String url = UriComponentsBuilder.fromHttpUrl(kisProperties.getBaseUrl())
                .path("/oauth2/tokenP")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "grant_type", "client_credentials",
                "appkey", kisProperties.getAppKey(),
                "appsecret", kisProperties.getAppSecret()
        );

        ResponseEntity<KisTokenResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                KisTokenResponse.class
        );

        KisTokenResponse tokenResponse = Objects.requireNonNull(response.getBody(), "KIS token response body is null");
        cachedAccessToken = tokenResponse.accessToken();
        tokenExpiresAt = Instant.now().plusSeconds(tokenResponse.expiresIn());
        return cachedAccessToken;
    }

    private HttpHeaders kisHeaders(String trId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken());
        headers.set("appkey", kisProperties.getAppKey());
        headers.set("appsecret", kisProperties.getAppSecret());
        headers.set("tr_id", trId);
        headers.set("custtype", "P");
        return headers;
    }

    private void validateProperties() {
        if (isBlank(kisProperties.getBaseUrl()) || isBlank(kisProperties.getAppKey()) || isBlank(kisProperties.getAppSecret())) {
            throw new IllegalStateException("KIS API settings are required: app.kis.base-url, app.kis.app-key, app.kis.app-secret");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    record KisTokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken,
            @com.fasterxml.jackson.annotation.JsonProperty("expires_in") long expiresIn
    ) {
    }

    record KisDailyChartResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("rt_cd") String rtCd,
            String msg1,
            KisDailySummary output1,
            List<KisDailyCandle> output2
    ) {
        KisDailyChartResponse {
            output2 = output2 == null ? List.of() : output2;
        }
    }

    record KisDailySummary(
            @com.fasterxml.jackson.annotation.JsonProperty("hts_kor_isnm") String stockName
    ) {
    }

    record KisDailyCandle(
            @com.fasterxml.jackson.annotation.JsonProperty("stck_bsop_date") String date,
            @com.fasterxml.jackson.annotation.JsonProperty("stck_oprc") String open,
            @com.fasterxml.jackson.annotation.JsonProperty("stck_hgpr") String high,
            @com.fasterxml.jackson.annotation.JsonProperty("stck_lwpr") String low,
            @com.fasterxml.jackson.annotation.JsonProperty("stck_clpr") String close,
            @com.fasterxml.jackson.annotation.JsonProperty("acml_vol") String volume
    ) {
        StockCandle toStockCandle() {
            return new StockCandle(
                    LocalDate.parse(date, KIS_DATE_FORMAT),
                    new BigDecimal(open),
                    new BigDecimal(high),
                    new BigDecimal(low),
                    new BigDecimal(close),
                    Long.parseLong(volume)
            );
        }
    }
}
