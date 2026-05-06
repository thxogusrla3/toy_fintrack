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
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockScreenerService {
    private final StockThemeRegistry stockThemeRegistry;
    private final StockCandleCollectService stockCandleCollectService;

    @Value("${app.stock.screener.request-delay-ms:1200}")
    private long requestDelayMs;

    @Value("${app.stock.screener.max-retries:2}")
    private int maxRetries;

    public List<ThemeCandidate> findThemes() {
        return stockThemeRegistry.findThemes();
    }

    public List<ThemeScreenerResult> run(String themeKey, LocalDate from, LocalDate to, int limit, boolean collect) {
        String selectedThemeKey = themeKey == null || themeKey.isBlank() ? "all" : themeKey;
        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDate startDate = from == null ? endDate.minusDays(90) : from;
        int candleLimit = limit <= 0 ? 60 : Math.min(limit, 60);

        List<ThemeCandidate> candidates = stockThemeRegistry.findCandidates(selectedThemeKey);
        List<ThemeScreenerResult> results = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            results.add(screen(candidates.get(i), startDate, endDate, candleLimit, collect));
            if (collect && i < candidates.size() - 1) {
                sleep(requestDelayMs);
            }
        }

        return results.stream()
                .sorted(Comparator.comparing(ThemeScreenerResult::score).reversed()
                .thenComparing(ThemeScreenerResult::themeName)
                .thenComparing(ThemeScreenerResult::stockName))
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
