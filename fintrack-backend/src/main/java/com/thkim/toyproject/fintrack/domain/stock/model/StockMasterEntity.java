package com.thkim.toyproject.fintrack.domain.stock.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock_master",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_master_code", columnNames = "stock_code"),
        indexes = {
                @Index(name = "ix_stock_master_name", columnList = "stock_name"),
                @Index(name = "ix_stock_master_theme_key", columnList = "theme_key")
        })
public class StockMasterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "theme_key", nullable = false, length = 100)
    private String themeKey;

    @Column(name = "theme_name", nullable = false, length = 100)
    private String themeName;

    @Column(name = "keywords", length = 1000)
    private String keywords;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static StockMasterEntity of(String stockCode, String stockName, String themeKey, String themeName, List<String> keywords) {
        StockMasterEntity entity = new StockMasterEntity();
        entity.stockCode = stockCode;
        entity.update(stockName, themeKey, themeName, keywords);
        return entity;
    }

    public void update(String stockName, String themeKey, String themeName, List<String> keywords) {
        this.stockName = stockName;
        this.themeKey = themeKey;
        this.themeName = themeName;
        this.keywords = String.join(",", keywords);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean matches(String text) {
        if (text.contains(stockName) || text.contains(themeName)) {
            return true;
        }
        return keywords().stream().anyMatch(text::contains);
    }

    public List<String> keywords() {
        if (keywords == null || keywords.isBlank()) {
            return List.of();
        }
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .toList();
    }

    public DiscoveredThemeStock toDiscoveredThemeStock() {
        return new DiscoveredThemeStock(stockCode, stockName);
    }
}
