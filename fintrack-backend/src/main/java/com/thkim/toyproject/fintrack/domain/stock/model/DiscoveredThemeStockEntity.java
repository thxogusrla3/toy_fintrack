package com.thkim.toyproject.fintrack.domain.stock.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "discovered_theme_stocks",
        uniqueConstraints = @UniqueConstraint(name = "uk_discovered_theme_stocks_theme_code", columnNames = {"theme_id", "stock_code"}))
public class DiscoveredThemeStockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private DiscoveredThemeEntity theme;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    public static DiscoveredThemeStockEntity of(DiscoveredThemeEntity theme, DiscoveredThemeStock stock) {
        DiscoveredThemeStockEntity entity = new DiscoveredThemeStockEntity();
        entity.theme = theme;
        entity.stockCode = stock.stockCode();
        entity.stockName = stock.stockName();
        return entity;
    }

    public void update(DiscoveredThemeStock stock) {
        this.stockName = stock.stockName();
    }

    public DiscoveredThemeStock toModel() {
        return new DiscoveredThemeStock(stockCode, stockName);
    }
}
