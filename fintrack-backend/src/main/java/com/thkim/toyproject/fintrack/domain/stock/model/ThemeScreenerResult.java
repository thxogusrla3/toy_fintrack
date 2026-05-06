package com.thkim.toyproject.fintrack.domain.stock.model;

import java.util.List;

public record ThemeScreenerResult(
        String themeKey,
        String themeName,
        String stockCode,
        String stockName,
        SignalType signal,
        boolean passed,
        int score,
        int collectedCount,
        List<String> reasons
) {
    public ThemeScreenerResult {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
