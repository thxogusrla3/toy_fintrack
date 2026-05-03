package com.thkim.toyproject.fintrack.application.api.stock;

import com.thkim.toyproject.fintrack.application.api.stock.dto.StockSignalRequest;
import com.thkim.toyproject.fintrack.domain.stock.StockSignalService;
import com.thkim.toyproject.fintrack.domain.stock.model.TradeSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks/signals")
public class StockSignalController {
    private final StockSignalService stockSignalService;

    @PostMapping("/analyze")
    public TradeSignal analyze(@Valid @RequestBody StockSignalRequest request) {
        return stockSignalService.analyze(request.toCandles());
    }
}
