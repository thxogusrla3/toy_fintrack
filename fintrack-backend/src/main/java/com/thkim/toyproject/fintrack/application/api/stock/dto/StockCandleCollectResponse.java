package com.thkim.toyproject.fintrack.application.api.stock.dto;

public record StockCandleCollectResponse(
        String stockCode,
        int collectedCount,
        long totalCount
) {
}
