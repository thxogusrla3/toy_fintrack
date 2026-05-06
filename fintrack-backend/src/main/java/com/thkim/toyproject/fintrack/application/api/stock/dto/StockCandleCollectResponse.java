package com.thkim.toyproject.fintrack.application.api.stock.dto;

public record StockCandleCollectResponse(
        String stockCode,
        String stockName,
        int collectedCount,
        long totalCount
) {
}
