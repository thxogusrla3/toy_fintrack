package com.thkim.toyproject.fintrack.application.api.stock.dto;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StockSignalRequest {
    @Valid
    @NotEmpty
    private List<StockCandleRequest> candles;

    public List<StockCandle> toCandles() {
        return candles.stream()
                .map(StockCandleRequest::toCandle)
                .toList();
    }
}
