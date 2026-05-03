package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;

import java.time.LocalDate;
import java.util.List;

public interface StockPriceProvider {
    List<StockCandle> getDailyCandles(String stockCode, LocalDate from, LocalDate to);
}
