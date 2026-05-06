package com.thkim.toyproject.fintrack.application.api.stock;

import com.thkim.toyproject.fintrack.domain.stock.StockThemeDiscoveryService;
import com.thkim.toyproject.fintrack.domain.stock.StockThemeTrendService;
import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeTrendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks/themes")
public class StockThemeTrendController {
    private final StockThemeTrendService stockThemeTrendService;
    private final StockThemeDiscoveryService stockThemeDiscoveryService;

    @GetMapping("/trending")
    public List<ThemeTrendResult> trending(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "true") boolean includeSearch
    ) {
        return stockThemeTrendService.findTrendingThemes(from, to, limit, includeSearch);
    }

    @GetMapping("/discover")
    public List<DiscoveredTheme> discover(@RequestParam(defaultValue = "10") int limit) {
        return stockThemeDiscoveryService.discoverThemes(limit);
    }

    @GetMapping("/discovered")
    public List<DiscoveredTheme> discovered(@RequestParam(defaultValue = "10") int limit) {
        return stockThemeDiscoveryService.findStoredThemes(limit);
    }
}
