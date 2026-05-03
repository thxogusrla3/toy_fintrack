package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
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
    public int collectDailyCandles(String stockCode, LocalDate from, LocalDate to) {
        List<StockCandle> candles = stockPriceProvider.getDailyCandles(stockCode, from, to);
        for (StockCandle candle : candles) {
            upsert(stockCode, candle);
        }
        return candles.size();
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

    private void upsert(String stockCode, StockCandle candle) {
        stockCandleRepository.findByStockCodeAndDate(stockCode, candle.date())
                .ifPresentOrElse(
                        entity -> entity.update(candle),
                        () -> stockCandleRepository.save(StockCandleEntity.of(stockCode, candle))
                );
    }
}
