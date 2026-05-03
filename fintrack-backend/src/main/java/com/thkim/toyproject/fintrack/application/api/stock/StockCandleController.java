package com.thkim.toyproject.fintrack.application.api.stock;

import com.thkim.toyproject.fintrack.application.api.stock.dto.StockCandleCollectResponse;
import com.thkim.toyproject.fintrack.domain.stock.StockCandleCollectService;
import com.thkim.toyproject.fintrack.domain.stock.model.TradeSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockCandleController {
    private final StockCandleCollectService stockCandleCollectService;

    @PostMapping("/{stockCode}/candles/collect")
    public StockCandleCollectResponse collectDailyCandles(
            @PathVariable String stockCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDate startDate = from == null ? endDate.minusDays(60) : from;

        int collectedCount = stockCandleCollectService.collectDailyCandles(stockCode, startDate, endDate);
        long totalCount = stockCandleCollectService.countCandles(stockCode);

        return new StockCandleCollectResponse(stockCode, collectedCount, totalCount);
    }

    @GetMapping("/{stockCode}/signals")
    public TradeSignal analyzeStoredCandles(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "60") int limit
    ) {
        return stockCandleCollectService.analyzeStoredCandles(stockCode, limit);
    }
}
