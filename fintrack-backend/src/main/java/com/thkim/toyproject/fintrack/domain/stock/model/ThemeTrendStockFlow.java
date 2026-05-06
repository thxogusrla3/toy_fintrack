package com.thkim.toyproject.fintrack.domain.stock.model;

public record ThemeTrendStockFlow(
        String stockCode,
        String stockName,
        double priceChangePercent,
        double volumeChangePercent,
        double turnover
) {
}
