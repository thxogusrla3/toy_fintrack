package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme;
import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredThemeEntity;
import com.thkim.toyproject.fintrack.domain.stock.repository.DiscoveredThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockThemeDiscoveryService {
    private final ThemeDiscoveryProvider themeDiscoveryProvider;
    private final DiscoveredThemeRepository discoveredThemeRepository;

    @Transactional
    public List<DiscoveredTheme> discoverThemes(int limit) {
        int resultLimit = limit <= 0 ? 10 : Math.min(limit, 30);
        List<DiscoveredTheme> discoveredThemes = themeDiscoveryProvider.discover(100).stream()
                .sorted(Comparator.comparing(DiscoveredTheme::score).reversed()
                        .thenComparing(DiscoveredTheme::mentionCount, Comparator.reverseOrder())
                .thenComparing(DiscoveredTheme::themeName))
                .limit(resultLimit)
                .toList();

        for (DiscoveredTheme discoveredTheme : discoveredThemes) {
            discoveredThemeRepository.findByThemeKey(discoveredTheme.themeKey())
                    .ifPresentOrElse(
                            entity -> entity.update(discoveredTheme),
                            () -> discoveredThemeRepository.save(DiscoveredThemeEntity.of(discoveredTheme))
                    );
        }
        return findStoredThemes(resultLimit);
    }

    public List<DiscoveredTheme> findStoredThemes(int limit) {
        int resultLimit = limit <= 0 ? 10 : Math.min(limit, 30);
        return discoveredThemeRepository.findTop30ByOrderByScoreDescMentionCountDescThemeNameAsc().stream()
                .map(DiscoveredThemeEntity::toModel)
                .limit(resultLimit)
                .toList();
    }

    public Optional<DiscoveredTheme> findStoredTheme(String themeKey) {
        String normalizedThemeKey = themeKey != null && themeKey.startsWith("discovered-")
                ? themeKey.substring("discovered-".length())
                : themeKey;
        return discoveredThemeRepository.findByThemeKey(normalizedThemeKey)
                .map(DiscoveredThemeEntity::toModel);
    }
}
