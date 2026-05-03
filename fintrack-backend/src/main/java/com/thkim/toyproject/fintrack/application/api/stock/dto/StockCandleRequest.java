package com.thkim.toyproject.fintrack.application.api.stock.dto;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class StockCandleRequest {
    @NotNull
    private LocalDate date;

    @NotNull
    private BigDecimal open;

    @NotNull
    private BigDecimal high;

    @NotNull
    private BigDecimal low;

    @NotNull
    private BigDecimal close;

    @PositiveOrZero
    private long volume;

    public StockCandle toCandle() {
        return new StockCandle(date, open, high, low, close, volume);
    }
}
