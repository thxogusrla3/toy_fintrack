package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.SignalType;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeCandidate;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeScreenerResult;
import com.thkim.toyproject.fintrack.domain.stock.model.TradeSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockScreenerService {
    private final StockMasterService stockMasterService;
    private final StockThemeDiscoveryService stockThemeDiscoveryService;
    private final StockCandleCollectService stockCandleCollectService;

    @Value("${app.stock.screener.request-delay-ms:1200}")
    private long requestDelayMs;

    @Value("${app.stock.screener.max-retries:2}")
    private int maxRetries;

    public List<ThemeCandidate> findThemes() {
        Map<String, ThemeCandidate> themes = new LinkedHashMap<>();
        for (ThemeCandidate theme : stockMasterService.findThemes()) {
            themes.put(theme.themeKey(), theme);
        }
        for (com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme theme : stockThemeDiscoveryService.findStoredThemes(30)) {
            String themeKey = "discovered-" + theme.themeKey();
            themes.putIfAbsent(themeKey, new ThemeCandidate(themeKey, theme.themeName(), "", ""));
        }
        return themes.values().stream()
                .sorted(Comparator.comparing(ThemeCandidate::themeName))
                .toList();
    }

    public List<ThemeScreenerResult> run(String themeKey, LocalDate from, LocalDate to, int limit, boolean collect) {
        return run(themeKey, from, to, limit, collect, null);
    }

    public List<ThemeScreenerResult> run(String themeKey, LocalDate from, LocalDate to, int limit, boolean collect, SignalType signal) {
        String selectedThemeKey = themeKey == null || themeKey.isBlank() ? "all" : themeKey;
        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDate startDate = from == null ? endDate.minusDays(90) : from;
        int candleLimit = limit <= 0 ? 60 : Math.min(limit, 60);

        List<ThemeCandidate> candidates = findCandidates(selectedThemeKey);
        List<ThemeScreenerResult> results = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            results.add(screen(candidates.get(i), startDate, endDate, candleLimit, collect));
            if (collect && i < candidates.size() - 1) {
                sleep(requestDelayMs);
            }
        }

        return results.stream()
                .filter(result -> signal == null || result.signal() == signal)
                .sorted(Comparator.comparing(ThemeScreenerResult::score).reversed()
                .thenComparing(ThemeScreenerResult::themeName)
                .thenComparing(ThemeScreenerResult::stockName))
                .toList();
    }

    private List<ThemeCandidate> findCandidates(String themeKey) {
        if (themeKey.startsWith("discovered-")) {
            return stockThemeDiscoveryService.findStoredTheme(themeKey)
                    .map(this::toCandidates)
                    .orElseGet(List::of);
        }
        if ("all".equals(themeKey)) {
            Map<String, ThemeCandidate> candidates = new LinkedHashMap<>();
            for (ThemeCandidate candidate : stockMasterService.findCandidates("all")) {
                candidates.put(candidate.stockCode(), candidate);
            }
            for (com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme theme : stockThemeDiscoveryService.findStoredThemes(30)) {
                for (ThemeCandidate candidate : toCandidates(theme)) {
                    candidates.putIfAbsent(candidate.stockCode(), candidate);
                }
            }
            return List.copyOf(candidates.values());
        }
        return stockMasterService.findCandidates(themeKey);
    }

    private List<ThemeCandidate> toCandidates(com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme theme) {
        String themeKey = "discovered-" + theme.themeKey();
        return theme.stocks().stream()
                .map(stock -> new ThemeCandidate(themeKey, theme.themeName(), stock.stockCode(), stock.stockName()))
                .sorted(Comparator.comparing(ThemeCandidate::stockName))
                .toList();
    }

    private ThemeScreenerResult screen(ThemeCandidate candidate, LocalDate from, LocalDate to, int limit, boolean collect) {
        int collectedCount = 0;
        try {
            if (collect) {
                collectedCount = collectWithRetry(candidate, from, to);
            }

            TradeSignal tradeSignal = stockCandleCollectService.analyzeStoredCandles(candidate.stockCode(), limit);
            int score = score(tradeSignal.signal());
            return new ThemeScreenerResult(
                    candidate.themeKey(),
                    candidate.themeName(),
                    candidate.stockCode(),
                    candidate.stockName(),
                    tradeSignal.signal(),
                    tradeSignal.signal() == SignalType.BUY || tradeSignal.signal() == SignalType.HOLD,
                    score,
                    collectedCount,
                    tradeSignal.reasons()
            );
        } catch (RuntimeException e) {
            return new ThemeScreenerResult(
                    candidate.themeKey(),
                    candidate.themeName(),
                    candidate.stockCode(),
                    candidate.stockName(),
                    SignalType.NONE,
                    false,
                    0,
                    collectedCount,
                    List.of("스크리너 실행 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    private int collectWithRetry(ThemeCandidate candidate, LocalDate from, LocalDate to) {
        RuntimeException lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return stockCandleCollectService.collectDailyCandles(candidate.stockCode(), from, to).collectedCount();
            } catch (RuntimeException e) {
                lastException = e;
                if (!isRateLimitError(e) || attempt == maxRetries) {
                    throw e;
                }
                sleep(requestDelayMs * (attempt + 2));
            }
        }
        throw lastException;
    }

    private boolean isRateLimitError(RuntimeException e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("초당") ||
                message.contains("거래건수") ||
                message.toLowerCase().contains("rate")
        );
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("스크리너 실행이 중단되었습니다.", e);
        }
    }

    private int score(SignalType signalType) {
        return switch (signalType) {
            case BUY -> 100;
            case HOLD -> 70;
            case NONE -> 30;
            case DANGER -> 0;
        };
    }
}
