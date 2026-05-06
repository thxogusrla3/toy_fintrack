package com.thkim.toyproject.fintrack.domain.stock.model;

import java.util.List;

public record DiscoveredTheme(
        String themeKey,
        String themeName,
        int score,
        int mentionCount,
        int matchedStockCount,
        List<String> matchedStocks,
        List<DiscoveredThemeStock> stocks,
        List<String> evidence
) {
    public DiscoveredTheme {
        matchedStocks = matchedStocks == null ? List.of() : List.copyOf(matchedStocks);
        stocks = stocks == null ? List.of() : List.copyOf(stocks);
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }
}
