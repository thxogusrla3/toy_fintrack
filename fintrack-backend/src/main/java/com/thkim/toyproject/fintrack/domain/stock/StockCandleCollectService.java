package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleCollectResult;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleSeries;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleEntity;
import com.thkim.toyproject.fintrack.domain.stock.model.TradeSignal;
import com.thkim.toyproject.fintrack.domain.stock.repository.StockCandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockCandleCollectService {
    private final StockPriceProvider stockPriceProvider;
    private final StockCandleRepository stockCandleRepository;
    private final StockSignalService stockSignalService;

    @Transactional
    public StockCandleCollectResult collectDailyCandles(String stockCode, LocalDate from, LocalDate to) {
        StockCandleSeries series = stockPriceProvider.getDailyCandleSeries(stockCode, from, to);
        for (StockCandle candle : series.candles()) {
            upsert(stockCode, series.stockName(), candle);
        }
        return new StockCandleCollectResult(series.stockName(), series.candles().size());
    }

    public TradeSignal analyzeStoredCandles(String stockCode, int limit) {
        List<StockCandle> candles = stockCandleRepository.findTop60ByStockCodeOrderByDateDesc(stockCode).stream()
                .limit(limit)
                .map(StockCandleEntity::toCandle)
                .sorted(Comparator.comparing(StockCandle::date))
                .toList();

        return stockSignalService.analyze(candles);
    }

    public long countCandles(String stockCode) {
        return stockCandleRepository.countByStockCode(stockCode);
    }

    private void upsert(String stockCode, String stockName, StockCandle candle) {
        stockCandleRepository.findByStockCodeAndDate(stockCode, candle.date())
                .ifPresentOrElse(
                        entity -> entity.update(stockName, candle),
                        () -> stockCandleRepository.save(StockCandleEntity.of(stockCode, stockName, candle))
                );
    }
}
