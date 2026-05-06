package com.thkim.toyproject.fintrack.domain.stock.model;

import java.util.List;

public record StockCandleSeries(
        String stockName,
        List<StockCandle> candles
) {
    public StockCandleSeries {
        candles = candles == null ? List.of() : List.copyOf(candles);
    }
}
