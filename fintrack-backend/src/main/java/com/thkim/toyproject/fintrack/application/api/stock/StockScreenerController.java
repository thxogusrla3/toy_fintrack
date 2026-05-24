package com.thkim.toyproject.fintrack.application.api.stock;

import com.thkim.toyproject.fintrack.domain.stock.StockScreenerService;
import com.thkim.toyproject.fintrack.domain.stock.model.SignalType;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeCandidate;
import com.thkim.toyproject.fintrack.domain.stock.model.ThemeScreenerResult;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks/screener")
public class StockScreenerController {
    private final StockScreenerService stockScreenerService;

    @GetMapping("/themes")
    public List<ThemeCandidate> themes() {
        return stockScreenerService.findThemes();
    }

    @PostMapping("/run")
    public List<ThemeScreenerResult> run(
            @RequestParam(defaultValue = "all") String themeKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "60") int limit,
            @RequestParam(defaultValue = "true") boolean collect,
            @RequestParam(required = false) SignalType signal
    ) {
        return stockScreenerService.run(themeKey, from, to, limit, collect, signal);
    }

    @GetMapping("/theme-signals")
    public List<ThemeScreenerResult> themeSignals(
            @RequestParam(defaultValue = "all") String themeKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "60") int limit,
            @RequestParam(defaultValue = "false") boolean collect,
            @RequestParam(required = false) SignalType signal
    ) {
        return stockScreenerService.run(themeKey, from, to, limit, collect, signal);
    }
}
