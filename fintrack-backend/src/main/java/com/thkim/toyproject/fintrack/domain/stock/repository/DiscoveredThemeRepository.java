package com.thkim.toyproject.fintrack.domain.stock.repository;

import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredThemeEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscoveredThemeRepository extends JpaRepository<DiscoveredThemeEntity, Long> {
    @EntityGraph(attributePaths = "stocks")
    Optional<DiscoveredThemeEntity> findByThemeKey(String themeKey);

    @EntityGraph(attributePaths = "stocks")
    List<DiscoveredThemeEntity> findTop30ByOrderByScoreDescMentionCountDescThemeNameAsc();
}
