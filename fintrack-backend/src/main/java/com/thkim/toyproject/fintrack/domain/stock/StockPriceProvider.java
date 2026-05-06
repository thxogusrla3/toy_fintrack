package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleSeries;

import java.time.LocalDate;
import java.util.List;

public interface StockPriceProvider {
    StockCandleSeries getDailyCandleSeries(String stockCode, LocalDate from, LocalDate to);

    default List<StockCandle> getDailyCandles(String stockCode, LocalDate from, LocalDate to) {
        return getDailyCandleSeries(stockCode, from, to).candles();
    }
}
