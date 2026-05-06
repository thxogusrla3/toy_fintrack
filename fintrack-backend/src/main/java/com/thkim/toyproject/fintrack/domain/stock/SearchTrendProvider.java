package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.ThemeCandidate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SearchTrendProvider {
    Map<String, Double> findSearchScores(List<ThemeCandidate> themes, LocalDate from, LocalDate to);
}
