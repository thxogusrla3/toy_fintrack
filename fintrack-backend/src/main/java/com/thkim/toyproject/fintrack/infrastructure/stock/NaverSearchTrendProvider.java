package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.SearchTrendProvider;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeCandidate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class NaverSearchTrendProvider implements SearchTrendProvider {
    private static final String NAVER_DATALAB_SEARCH_URL = "https://openapi.naver.com/v1/datalab/search";

    private final RestTemplate restTemplate;

    @Value("${app.naver.client-id:}")
    private String clientId;

    @Value("${app.naver.client-secret:}")
    private String clientSecret;

    public NaverSearchTrendProvider(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Map<String, Double> findSearchScores(List<ThemeCandidate> themes, LocalDate from, LocalDate to) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            return Map.of();
        }

        Map<String, Double> scores = new LinkedHashMap<>();
        for (List<ThemeCandidate> chunk : chunks(themes, 5)) {
            scores.putAll(request(chunk, from, to));
        }
        return scores;
    }

    private Map<String, Double> request(List<ThemeCandidate> themes, LocalDate from, LocalDate to) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        NaverTrendRequest request = new NaverTrendRequest(
                from.toString(),
                to.toString(),
                "date",
                themes.stream().map(this::keywordGroup).toList()
        );

        ResponseEntity<NaverTrendResponse> response = restTemplate.exchange(
                NAVER_DATALAB_SEARCH_URL,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                NaverTrendResponse.class
        );

        NaverTrendResponse body = Objects.requireNonNull(response.getBody(), "Naver trend response body is null");
        Map<String, Double> scores = new LinkedHashMap<>();
        for (NaverTrendResult result : body.results()) {
            double latest = result.data().isEmpty() ? 0 : result.data().get(result.data().size() - 1).ratio();
            double average = result.data().stream().mapToDouble(NaverTrendData::ratio).average().orElse(0);
            scores.put(result.title(), Math.max(latest, average));
        }
        return scores;
    }

    private NaverKeywordGroup keywordGroup(ThemeCandidate theme) {
        return new NaverKeywordGroup(theme.themeKey(), keywords(theme));
    }

    private List<String> keywords(ThemeCandidate theme) {
        return switch (theme.themeKey()) {
            case "ai-semiconductor" -> List.of("AI 반도체", "HBM", "반도체", "엔비디아", "온디바이스 AI");
            case "shipbuilding" -> List.of("조선", "조선주", "LNG선", "방산 선박", "수주");
            case "bio" -> List.of("제약 바이오", "바이오", "신약", "임상", "셀트리온");
            case "power-robot" -> List.of("전력기기", "로봇", "전력망", "변압기", "휴머노이드");
            default -> List.of(theme.themeName(), theme.stockName());
        };
    }

    private List<List<ThemeCandidate>> chunks(List<ThemeCandidate> themes, int size) {
        List<List<ThemeCandidate>> chunks = new ArrayList<>();
        for (int index = 0; index < themes.size(); index += size) {
            chunks.add(themes.subList(index, Math.min(index + size, themes.size())));
        }
        return chunks;
    }

    record NaverTrendRequest(
            String startDate,
            String endDate,
            String timeUnit,
            List<NaverKeywordGroup> keywordGroups
    ) {
    }

    record NaverKeywordGroup(
            String groupName,
            List<String> keywords
    ) {
    }

    record NaverTrendResponse(
            String startDate,
            String endDate,
            String timeUnit,
            List<NaverTrendResult> results
    ) {
        NaverTrendResponse {
            results = results == null ? List.of() : results;
        }
    }

    record NaverTrendResult(
            String title,
            List<NaverTrendData> data
    ) {
        NaverTrendResult {
            data = data == null ? List.of() : data;
        }
    }

    record NaverTrendData(
            String period,
            double ratio
    ) {
    }
}
