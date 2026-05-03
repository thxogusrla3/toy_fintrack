package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.StockPriceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "app.stock.provider=mock")
class MockStockPriceProviderConfigTest {
    @Autowired
    StockPriceProvider stockPriceProvider;

    @Test
    void mock_provider_is_selected() {
        assertThat(stockPriceProvider).isInstanceOf(MockStockPriceProvider.class);
    }
}
