package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme;

import java.util.List;

public interface ThemeDiscoveryProvider {
    List<DiscoveredTheme> discover(int display);
}
