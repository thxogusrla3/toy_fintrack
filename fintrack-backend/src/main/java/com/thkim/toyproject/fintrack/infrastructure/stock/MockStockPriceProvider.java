package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.StockPriceProvider;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleSeries;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.stock.provider", havingValue = "mock", matchIfMissing = true)
public class MockStockPriceProvider implements StockPriceProvider {
    @Override
    public StockCandleSeries getDailyCandleSeries(String stockCode, LocalDate from, LocalDate to) {
        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDate startDate = endDate.minusDays(30);
        if (from != null && from.isAfter(startDate)) {
            startDate = from;
        }

        LocalDate filterStartDate = startDate;
        LocalDate filterEndDate = endDate;
        List<StockCandle> candles = buySignalCandles(endDate);
        List<StockCandle> filteredCandles = candles.stream()
                .filter(candle -> !candle.date().isBefore(filterStartDate) && !candle.date().isAfter(filterEndDate))
                .toList();
        return new StockCandleSeries("Mock Stock", filteredCandles);
    }

    private List<StockCandle> buySignalCandles(LocalDate endDate) {
        List<StockCandle> candles = new ArrayList<>();
        LocalDate baseDate = endDate.minusDays(20);

        for (int i = 0; i < 19; i++) {
            BigDecimal close = BigDecimal.valueOf(100 + i);
            candles.add(candle(baseDate.plusDays(i), close, close.add(BigDecimal.ONE), close.subtract(BigDecimal.ONE), close, 1000));
        }

        candles.add(candle(baseDate.plusDays(19), "118", "118", "115", "116", 700));
        candles.add(candle(baseDate.plusDays(20), "116", "121", "115", "120", 2000));
        return candles;
    }

    private StockCandle candle(LocalDate date, String open, String high, String low, String close, long volume) {
        return candle(date, new BigDecimal(open), new BigDecimal(high), new BigDecimal(low), new BigDecimal(close), volume);
    }

    private StockCandle candle(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {
        return new StockCandle(date, open, high, low, close, volume);
    }
}
