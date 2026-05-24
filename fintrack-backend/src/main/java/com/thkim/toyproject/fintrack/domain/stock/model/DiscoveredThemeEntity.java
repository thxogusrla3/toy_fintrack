package com.thkim.toyproject.fintrack.domain.stock.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "discovered_themes",
        uniqueConstraints = @UniqueConstraint(name = "uk_discovered_themes_key", columnNames = "theme_key"),
        indexes = @Index(name = "ix_discovered_themes_score", columnList = "score"))
public class DiscoveredThemeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theme_key", nullable = false, length = 100)
    private String themeKey;

    @Column(name = "theme_name", nullable = false, length = 100)
    private String themeName;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "mention_count", nullable = false)
    private int mentionCount;

    @Column(name = "evidence", length = 2000)
    private String evidence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscoveredThemeStockEntity> stocks = new ArrayList<>();

    public static DiscoveredThemeEntity of(DiscoveredTheme theme) {
        DiscoveredThemeEntity entity = new DiscoveredThemeEntity();
        entity.themeKey = theme.themeKey();
        entity.createdAt = LocalDateTime.now();
        entity.update(theme);
        return entity;
    }

    public void update(DiscoveredTheme theme) {
        this.themeName = theme.themeName();
        this.score = theme.score();
        this.mentionCount = theme.mentionCount();
        this.evidence = String.join("\n", theme.evidence());
        this.updatedAt = LocalDateTime.now();

        Map<String, DiscoveredThemeStock> incomingStocks = new LinkedHashMap<>();
        for (DiscoveredThemeStock stock : theme.stocks()) {
            incomingStocks.put(stock.stockCode(), stock);
        }

        this.stocks.removeIf(stock -> !incomingStocks.containsKey(stock.getStockCode()));
        Map<String, DiscoveredThemeStockEntity> existingStocks = new LinkedHashMap<>();
        for (DiscoveredThemeStockEntity stock : this.stocks) {
            existingStocks.put(stock.getStockCode(), stock);
        }

        for (DiscoveredThemeStock stock : incomingStocks.values()) {
            DiscoveredThemeStockEntity existingStock = existingStocks.get(stock.stockCode());
            if (existingStock == null) {
                this.stocks.add(DiscoveredThemeStockEntity.of(this, stock));
            } else {
                existingStock.update(stock);
            }
        }
    }

    public DiscoveredTheme toModel() {
        List<DiscoveredThemeStock> stockModels = stocks.stream()
                .map(DiscoveredThemeStockEntity::toModel)
                .toList();
        List<String> matchedStockNames = stockModels.stream()
                .map(DiscoveredThemeStock::stockName)
                .toList();
        List<String> evidenceItems = evidence == null || evidence.isBlank()
                ? List.of()
                : List.of(evidence.split("\\n"));
        return new DiscoveredTheme(
                themeKey,
                themeName,
                score,
                mentionCount,
                stockModels.size(),
                matchedStockNames,
                stockModels,
                evidenceItems
        );
    }
}
