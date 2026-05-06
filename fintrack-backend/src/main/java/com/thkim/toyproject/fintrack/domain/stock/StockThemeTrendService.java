package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleSeries;
import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeCandidate;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeTrendResult;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeTrendStockFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockThemeTrendService {
    private final StockMasterService stockMasterService;
    private final StockPriceProvider stockPriceProvider;
    private final SearchTrendProvider searchTrendProvider;
    private final StockThemeDiscoveryService stockThemeDiscoveryService;

    @Value("${app.stock.trends.request-delay-ms:300}")
    private long requestDelayMs;

    public List<ThemeTrendResult> findTrendingThemes(LocalDate from, LocalDate to, int limit, boolean includeSearch) {
        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDate startDate = from == null ? endDate.minusDays(14) : from;
        int resultLimit = limit <= 0 ? 10 : Math.min(limit, 30);

        Map<String, List<ThemeCandidate>> groupedCandidates = groupByTheme();
        List<ThemeCandidate> themes = buildThemes(groupedCandidates);
        Map<String, Double> searchScores = includeSearch
                ? searchTrendProvider.findSearchScores(themes, startDate, endDate)
                : Map.of();

        List<ThemeTrendResult> results = new ArrayList<>();
        int index = 0;
        for (ThemeCandidate theme : themes) {
            List<ThemeCandidate> candidates = groupedCandidates.getOrDefault(theme.themeKey(), List.of());
            results.add(analyzeTheme(theme, candidates, searchScores.getOrDefault(theme.themeKey(), 0.0), startDate, endDate));
            if (++index < themes.size()) {
                sleep(requestDelayMs);
            }
        }

        return results.stream()
                .sorted(Comparator.comparing(ThemeTrendResult::totalScore).reversed()
                        .thenComparing(ThemeTrendResult::turnover, Comparator.reverseOrder())
                        .thenComparing(ThemeTrendResult::themeName))
                .limit(resultLimit)
                .toList();
    }

    private Map<String, List<ThemeCandidate>> groupByTheme() {
        Map<String, List<ThemeCandidate>> grouped = new LinkedHashMap<>();
        for (ThemeCandidate candidate : stockMasterService.findCandidates("all")) {
            grouped.computeIfAbsent(candidate.themeKey(), ignored -> new ArrayList<>()).add(candidate);
        }
        for (DiscoveredTheme discoveredTheme : stockThemeDiscoveryService.findStoredThemes(30)) {
            if (discoveredTheme.stocks().isEmpty()) {
                continue;
            }
            String themeKey = discoveredThemeKey(discoveredTheme);
            List<ThemeCandidate> candidates = discoveredTheme.stocks().stream()
                    .map(stock -> new ThemeCandidate(themeKey, discoveredTheme.themeName(), stock.stockCode(), stock.stockName()))
                    .toList();
            grouped.put(themeKey, candidates);
        }
        return grouped;
    }

    private List<ThemeCandidate> buildThemes(Map<String, List<ThemeCandidate>> groupedCandidates) {
        return groupedCandidates.values().stream()
                .filter(candidates -> !candidates.isEmpty())
                .map(candidates -> candidates.get(0))
                .sorted(Comparator.comparing(ThemeCandidate::themeName))
                .toList();
    }

    private String discoveredThemeKey(DiscoveredTheme discoveredTheme) {
        return "discovered-" + discoveredTheme.themeKey();
    }

    private ThemeTrendResult analyzeTheme(
            ThemeCandidate theme,
            List<ThemeCandidate> candidates,
            double searchScore,
            LocalDate from,
            LocalDate to
    ) {
        List<ThemeTrendStockFlow> stockFlows = candidates.stream()
                .map(candidate -> analyzeStock(candidate, from, to))
                .filter(flow -> flow != null)
                .sorted(Comparator.comparing(ThemeTrendStockFlow::turnover).reversed())
                .toList();

        double averagePriceChange = stockFlows.stream()
                .mapToDouble(ThemeTrendStockFlow::priceChangePercent)
                .average()
                .orElse(0);
        double averageVolumeChange = stockFlows.stream()
                .mapToDouble(ThemeTrendStockFlow::volumeChangePercent)
                .average()
                .orElse(0);
        double turnover = stockFlows.stream()
                .mapToDouble(ThemeTrendStockFlow::turnover)
                .sum();

        int normalizedSearchScore = clamp(searchScore);
        int flowScore = clamp(averageVolumeChange * 0.6 + Math.log10(Math.max(turnover, 1)) * 6);
        int priceScore = clamp(averagePriceChange * 8);
        int totalScore = clamp(normalizedSearchScore * 0.35 + flowScore * 0.4 + priceScore * 0.25);

        List<String> reasons = new ArrayList<>();
        reasons.add("검색 관심도 " + normalizedSearchScore + "점");
        reasons.add("평균 등락률 " + round(averagePriceChange) + "%");
        reasons.add("평균 거래량 변화 " + round(averageVolumeChange) + "%");

        return new ThemeTrendResult(
                theme.themeKey(),
                theme.themeName(),
                totalScore,
                normalizedSearchScore,
                flowScore,
                priceScore,
                round(averagePriceChange),
                round(averageVolumeChange),
                round(turnover),
                stockFlows,
                reasons
        );
    }

    private ThemeTrendStockFlow analyzeStock(ThemeCandidate candidate, LocalDate from, LocalDate to) {
        try {
            StockCandleSeries series = stockPriceProvider.getDailyCandleSeries(candidate.stockCode(), from, to);
            List<StockCandle> candles = series.candles();
            if (candles.size() < 2) {
                return null;
            }

            StockCandle first = candles.get(0);
            StockCandle latest = candles.get(candles.size() - 1);
            double priceChange = percentChange(first.close(), latest.close());
            double averageVolume = candles.stream()
                    .limit(Math.max(1, candles.size() - 1))
                    .mapToLong(StockCandle::volume)
                    .average()
                    .orElse(latest.volume());
            double volumeChange = averageVolume == 0 ? 0 : ((latest.volume() - averageVolume) / averageVolume) * 100;
            double turnover = latest.close().doubleValue() * latest.volume();

            return new ThemeTrendStockFlow(
                    candidate.stockCode(),
                    candidate.stockName(),
                    round(priceChange),
                    round(volumeChange),
                    round(turnover)
            );
        } catch (RuntimeException e) {
            return null;
        }
    }

    private double percentChange(BigDecimal base, BigDecimal current) {
        if (base == null || current == null || BigDecimal.ZERO.compareTo(base) == 0) {
            return 0;
        }
        return current.subtract(base)
                .divide(base, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private int clamp(double value) {
        return (int) Math.max(0, Math.min(100, Math.round(value)));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("테마 트렌드 분석이 중단되었습니다.", e);
        }
    }
}
