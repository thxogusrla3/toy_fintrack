package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.StockMasterService;
import com.thkim.toyproject.fintrack.domain.stock.ThemeDiscoveryProvider;
import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme;
import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredThemeStock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NaverNewsThemeDiscoveryProvider implements ThemeDiscoveryProvider {
    private static final String NAVER_NEWS_SEARCH_URL = "https://openapi.naver.com/v1/search/news.json";
    private static final Pattern THEME_PATTERN = Pattern.compile("([가-힣A-Za-z0-9·\\-\\s]{2,18})(?:관련주|테마주|테마|주도주)");
    private static final List<String> THEME_KEYWORDS = List.of(
            "AI 반도체", "HBM", "온디바이스 AI", "반도체",
            "2차전지", "전고체", "배터리", "전기차",
            "로봇", "휴머노이드", "전력기기", "전력망", "변압기",
            "원전", "SMR", "방산", "조선", "LNG선",
            "바이오", "제약", "비만치료제", "화장품",
            "저출산", "교육", "게임", "엔터", "웹툰",
            "가상자산", "보안", "양자", "우주항공"
    );
    private static final List<String> SEARCH_QUERIES = List.of(
            "증시 주도 테마",
            "주식 테마 관련주",
            "코스피 코스닥 급등 테마",
            "거래대금 상위 테마",
            "오늘 증시 테마",
            "AI 반도체 관련주",
            "2차전지 관련주",
            "로봇 관련주",
            "전력기기 관련주",
            "원전 관련주",
            "방산 관련주",
            "조선 관련주",
            "바이오 관련주"
    );
    private static final Set<String> STOP_WORDS = Set.of(
            "주식", "증시", "오늘", "국내", "코스피", "코스닥", "상승", "하락", "급등", "급락",
            "거래대금", "외국인", "기관", "개인", "관련", "테마", "종목", "시장"
    );
    private static final List<String> STOCK_CONTEXT_KEYWORDS = List.of(
            "증시", "주식", "관련주", "테마주", "주도주", "급등", "상한가", "코스피", "코스닥",
            "거래대금", "종목", "외국인", "기관", "매수", "매도"
    );

    private final RestTemplate restTemplate;
    private final StockMasterService stockMasterService;

    @Value("${app.naver.client-id:}")
    private String clientId;

    @Value("${app.naver.client-secret:}")
    private String clientSecret;

    @Value("${app.naver.news.max-requests:8}")
    private int maxRequests;

    @Value("${app.naver.news.request-delay-ms:250}")
    private long requestDelayMs;

    public NaverNewsThemeDiscoveryProvider(RestTemplateBuilder restTemplateBuilder, StockMasterService stockMasterService) {
        this.restTemplate = restTemplateBuilder.build();
        this.stockMasterService = stockMasterService;
    }

    @Override
    public List<DiscoveredTheme> discover(int display) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            return List.of();
        }

        Map<String, ThemeBucket> buckets = new LinkedHashMap<>();
        int requestCount = 0;
        for (String query : SEARCH_QUERIES.stream().limit(Math.max(1, maxRequests)).toList()) {
            List<NaverNewsItem> newsItems;
            try {
                newsItems = search(query, displayPerQuery(display));
            } catch (HttpClientErrorException.TooManyRequests e) {
                break;
            }
            requestCount++;

            String seedTheme = seedThemeFromQuery(query);
            if (seedTheme != null) {
                addSeedThemeCandidate(buckets, seedTheme, newsItems);
            }

            for (NaverNewsItem item : newsItems) {
                String title = clean(item.title());
                String description = clean(item.description());
                String text = title + " " + description;
                if (!isStockMarketArticle(text)) {
                    continue;
                }
                Set<String> themes = extractThemes(text);
                if (seedTheme != null) {
                    themes.add(seedTheme);
                }
                List<DiscoveredThemeStock> matchedStocks = matchStocks(text);
                for (String theme : themes) {
                    ThemeBucket bucket = buckets.computeIfAbsent(themeKey(theme), key -> new ThemeBucket(theme));
                    bucket.mentionCount++;
                    bucket.addStocks(matchedStocks);
                    if (bucket.matchedStocks.isEmpty()) {
                        bucket.addStocks(inferThemeStocks(theme));
                    }
                    if (bucket.evidence.size() < 3) {
                        bucket.evidence.add(title);
                    }
                }
            }
            if (requestCount < maxRequests) {
                sleep(requestDelayMs);
            }
        }

        return buckets.values().stream()
                .map(ThemeBucket::toDiscoveredTheme)
                .filter(theme -> theme.mentionCount() > 0)
                .sorted(Comparator.comparing(DiscoveredTheme::score).reversed())
                .toList();
    }

    private void addSeedThemeCandidate(Map<String, ThemeBucket> buckets, String seedTheme, List<NaverNewsItem> newsItems) {
        if (newsItems.isEmpty()) {
            return;
        }

        ThemeBucket bucket = buckets.computeIfAbsent(themeKey(seedTheme), key -> new ThemeBucket(seedTheme));
        bucket.mentionCount += Math.min(3, newsItems.size());
        for (NaverNewsItem item : newsItems) {
            String title = clean(item.title());
            String description = clean(item.description());
            String text = title + " " + description;
            bucket.addStocks(matchStocks(text));
            if (bucket.evidence.size() < 3) {
                bucket.evidence.add(title);
            }
        }
        if (bucket.matchedStocks.isEmpty()) {
            bucket.addStocks(inferThemeStocks(seedTheme));
        }
    }

    private List<NaverNewsItem> search(String query, int display) {
        String url = UriComponentsBuilder.fromHttpUrl(NAVER_NEWS_SEARCH_URL)
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("sort", "date")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        ResponseEntity<NaverNewsResponse> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    NaverNewsResponse.class
            );
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalStateException("네이버 뉴스 검색 API 인증에 실패했습니다. NAVER_CLIENT_ID/NAVER_CLIENT_SECRET 값과 네이버 개발자센터의 검색 API 권한을 확인하세요.", e);
        } catch (HttpClientErrorException.Forbidden e) {
            throw new IllegalStateException("네이버 뉴스 검색 API 접근이 거부되었습니다. 네이버 개발자센터에서 검색 API 사용 권한이 활성화되어 있는지 확인하세요.", e);
        }

        NaverNewsResponse body = Objects.requireNonNull(response.getBody(), "Naver news response body is null");
        return body.items();
    }

    private int displayPerQuery(int display) {
        return Math.max(3, Math.min(8, display / Math.max(1, maxRequests)));
    }

    private Set<String> extractThemes(String text) {
        Set<String> themes = new LinkedHashSet<>();
        for (String keyword : THEME_KEYWORDS) {
            if (text.contains(keyword)) {
                themes.add(normalizeThemeName(keyword));
            }
        }

        Matcher matcher = THEME_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = normalizeThemeName(matcher.group(1));
            if (isUsefulTheme(candidate)) {
                themes.add(candidate);
            }
        }
        return themes;
    }

    private boolean isStockMarketArticle(String text) {
        return STOCK_CONTEXT_KEYWORDS.stream().anyMatch(text::contains);
    }

    private String seedThemeFromQuery(String query) {
        for (String keyword : THEME_KEYWORDS) {
            if (query.contains(keyword)) {
                return normalizeThemeName(keyword);
            }
        }
        return null;
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("네이버 뉴스 테마 후보 수집이 중단되었습니다.", e);
        }
    }

    private List<DiscoveredThemeStock> matchStocks(String text) {
        return stockMasterService.matchStocks(text);
    }

    private List<DiscoveredThemeStock> inferThemeStocks(String themeName) {
        return stockMasterService.findThemeStocks(themeName);
    }

    private boolean isUsefulTheme(String theme) {
        if (theme.length() < 2 || theme.length() > 18) {
            return false;
        }
        String compact = theme.replace(" ", "");
        return !STOP_WORDS.contains(compact) && STOP_WORDS.stream().noneMatch(stopWord -> compact.equals(stopWord + "주"));
    }

    private String normalizeThemeName(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replaceAll("<[^>]*>", "")
                .replaceAll("[^가-힣A-Za-z0-9·\\-\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.endsWith("주")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private String themeKey(String themeName) {
        return themeName.toLowerCase(Locale.ROOT)
                .replace("·", "-")
                .replaceAll("[^가-힣a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("<[^>]*>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&#39;", "'")
                .trim();
    }

    record NaverNewsResponse(
            List<NaverNewsItem> items
    ) {
        NaverNewsResponse {
            items = items == null ? List.of() : items;
        }
    }

    record NaverNewsItem(
            String title,
            String description,
            String pubDate
    ) {
    }

    private static class ThemeBucket {
        private final String themeName;
        private int mentionCount;
        private final Map<String, DiscoveredThemeStock> matchedStocks = new LinkedHashMap<>();
        private final List<String> evidence = new ArrayList<>();

        private ThemeBucket(String themeName) {
            this.themeName = themeName;
        }

        private void addStocks(List<DiscoveredThemeStock> stocks) {
            for (DiscoveredThemeStock stock : stocks) {
                matchedStocks.put(stock.stockCode(), stock);
            }
        }

        private DiscoveredTheme toDiscoveredTheme() {
            int score = Math.min(100, mentionCount * 18 + matchedStocks.size() * 8);
            List<DiscoveredThemeStock> stocks = List.copyOf(matchedStocks.values());
            return new DiscoveredTheme(
                    themeName.toLowerCase(Locale.ROOT).replaceAll("[^가-힣a-z0-9]+", "-"),
                    themeName,
                    score,
                    mentionCount,
                    matchedStocks.size(),
                    stocks.stream().map(DiscoveredThemeStock::stockName).toList(),
                    stocks,
                    List.copyOf(evidence)
            );
        }
    }
}
