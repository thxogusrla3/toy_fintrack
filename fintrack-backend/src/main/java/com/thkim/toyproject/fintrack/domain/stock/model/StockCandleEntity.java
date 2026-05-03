package com.thkim.toyproject.fintrack.domain.stock.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_candles",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_candles_code_date", columnNames = {"stock_code", "date"}),
        indexes = @Index(name = "ix_stock_candles_code_date", columnList = "stock_code, date"))
public class StockCandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "open", nullable = false, precision = 19, scale = 2)
    private BigDecimal open;

    @Column(name = "high", nullable = false, precision = 19, scale = 2)
    private BigDecimal high;

    @Column(name = "low", nullable = false, precision = 19, scale = 2)
    private BigDecimal low;

    @Column(name = "close", nullable = false, precision = 19, scale = 2)
    private BigDecimal close;

    @Column(name = "volume", nullable = false)
    private Long volume;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static StockCandleEntity of(String stockCode, StockCandle candle) {
        StockCandleEntity entity = new StockCandleEntity();
        entity.stockCode = stockCode;
        entity.date = candle.date();
        entity.createdAt = LocalDateTime.now();
        entity.update(candle);
        return entity;
    }

    public void update(StockCandle candle) {
        this.open = candle.open();
        this.high = candle.high();
        this.low = candle.low();
        this.close = candle.close();
        this.volume = candle.volume();
        this.updatedAt = LocalDateTime.now();
    }

    public StockCandle toCandle() {
        return new StockCandle(date, open, high, low, close, volume);
    }
}
