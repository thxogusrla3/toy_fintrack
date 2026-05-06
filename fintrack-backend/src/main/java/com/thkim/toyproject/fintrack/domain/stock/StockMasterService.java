package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredThemeStock;
import com.thkim.toyproject.fintrack.domain.stock.model.StockMasterEntity;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeCandidate;
import com.thkim.toyproject.fintrack.domain.stock.repository.StockMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockMasterService {
    private final StockMasterRepository stockMasterRepository;

    public List<DiscoveredThemeStock> matchStocks(String text) {
        Map<String, DiscoveredThemeStock> stocks = new LinkedHashMap<>();
        for (StockMasterEntity stock : stockMasterRepository.findAll()) {
            if (stock.matches(text)) {
                stocks.put(stock.getStockCode(), stock.toDiscoveredThemeStock());
            }
        }
        return List.copyOf(stocks.values());
    }

    public List<DiscoveredThemeStock> findThemeStocks(String themeName) {
        String themeKey = inferThemeKey(themeName);
        if (themeKey == null) {
            return List.of();
        }
        return stockMasterRepository.findByThemeKey(themeKey).stream()
                .map(StockMasterEntity::toDiscoveredThemeStock)
                .toList();
    }

    public List<ThemeCandidate> findThemes() {
        return stockMasterRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        StockMasterEntity::getThemeKey,
                        stock -> new ThemeCandidate(stock.getThemeKey(), stock.getThemeName(), stock.getStockCode(), stock.getStockName()),
                        (left, right) -> left
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(ThemeCandidate::themeName))
                .toList();
    }

    public List<ThemeCandidate> findCandidates(String themeKey) {
        return stockMasterRepository.findAll().stream()
                .filter(stock -> themeKey == null || "all".equals(themeKey) || stock.getThemeKey().equals(themeKey))
                .map(stock -> new ThemeCandidate(stock.getThemeKey(), stock.getThemeName(), stock.getStockCode(), stock.getStockName()))
                .sorted(Comparator.comparing(ThemeCandidate::themeName).thenComparing(ThemeCandidate::stockName))
                .toList();
    }

    private String inferThemeKey(String themeName) {
        String compact = themeName.replace(" ", "");
        if (compact.contains("AI") || compact.contains("HBM") || compact.contains("반도체")) {
            return "ai-semiconductor";
        }
        if (compact.contains("2차전지") || compact.contains("전고체") || compact.contains("배터리") || compact.contains("전기차")) {
            return "battery";
        }
        if (compact.contains("조선") || compact.contains("LNG")) {
            return "shipbuilding";
        }
        if (compact.contains("바이오") || compact.contains("제약") || compact.contains("비만치료제")) {
            return "bio";
        }
        if (compact.contains("로봇") || compact.contains("전력") || compact.contains("변압기")) {
            return "power-robot";
        }
        if (compact.contains("원전") || compact.contains("SMR")) {
            return "nuclear";
        }
        if (compact.contains("방산")) {
            return "defense";
        }
        if (compact.contains("화장품")) {
            return "cosmetics";
        }
        if (compact.contains("가상자산") || compact.contains("보안")) {
            return "crypto-security";
        }
        return null;
    }
}
