package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredTheme;
import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredThemeEntity;
import com.thkim.toyproject.fintrack.domain.stock.model.DiscoveredThemeStock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveredThemeEntityTest {
    @Test
    void updateMergesStocksByStockCode() {
        DiscoveredThemeEntity entity = DiscoveredThemeEntity.of(theme(
                List.of(new DiscoveredThemeStock("267260", "HD현대일렉트릭"))
        ));

        entity.update(theme(List.of(
                new DiscoveredThemeStock("267260", "HD현대일렉트릭"),
                new DiscoveredThemeStock("267260", "HD현대일렉트릭 변경")
        )));

        assertThat(entity.getStocks()).hasSize(1);
        assertThat(entity.getStocks().get(0).getStockCode()).isEqualTo("267260");
        assertThat(entity.getStocks().get(0).getStockName()).isEqualTo("HD현대일렉트릭 변경");
    }

    private DiscoveredTheme theme(List<DiscoveredThemeStock> stocks) {
        return new DiscoveredTheme(
                "power-equipment",
                "전력기기",
                100,
                3,
                stocks.size(),
                stocks.stream().map(DiscoveredThemeStock::stockName).toList(),
                stocks,
                List.of("전력기기 관련주 강세")
        );
    }
}
