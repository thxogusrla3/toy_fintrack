package com.thkim.toyproject.fintrack.domain.stock.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockCandle(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume
) {
}
