package com.thkim.toyproject.fintrack.domain.stock.model;

import java.util.List;

public record ThemeTrendResult(
        String themeKey,
        String themeName,
        int totalScore,
        int searchScore,
        int flowScore,
        int priceScore,
        double averagePriceChangePercent,
        double averageVolumeChangePercent,
        double turnover,
        List<ThemeTrendStockFlow> stocks,
        List<String> reasons
) {
    public ThemeTrendResult {
        stocks = stocks == null ? List.of() : List.copyOf(stocks);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
