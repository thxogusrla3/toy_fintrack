package com.thkim.toyproject.fintrack.infrastructure.stock;

import com.thkim.toyproject.fintrack.domain.stock.StockPriceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "app.stock.provider=kis")
class KisStockPriceProviderConfigTest {
    @Autowired
    StockPriceProvider stockPriceProvider;

    @Test
    void kis_provider_is_selected() {
        assertThat(stockPriceProvider).isInstanceOf(KisStockPriceProvider.class);
    }
}
