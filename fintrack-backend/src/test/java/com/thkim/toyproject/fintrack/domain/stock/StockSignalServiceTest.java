package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.SignalType;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import com.thkim.toyproject.fintrack.domain.stock.model.TradeSignal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockSignalServiceTest {
    private final StockSignalService service = new StockSignalService();

    @Test
    void analyze_returns_buy_when_pullback_rebounds_with_volume() {
        List<StockCandle> candles = new ArrayList<>();
        LocalDate baseDate = LocalDate.of(2026, 1, 1);

        for (int i = 0; i < 19; i++) {
            String close = String.valueOf(100 + i);
            candles.add(candle(baseDate.plusDays(i), close, String.valueOf(101 + i), String.valueOf(99 + i), close, 1000));
        }
        candles.add(candle(baseDate.plusDays(19), "118", "118", "115", "116", 700));
        candles.add(candle(baseDate.plusDays(20), "116", "121", "115", "120", 2000));

        TradeSignal signal = service.analyze(candles);

        assertThat(signal.signal()).isEqualTo(SignalType.BUY);
        assertThat(signal.reasons()).contains("전일 고가를 돌파했습니다.");
    }

    @Test
    void analyze_returns_hold_when_uptrend_continues() {
        List<StockCandle> candles = new ArrayList<>();
        LocalDate baseDate = LocalDate.of(2026, 1, 1);

        for (int i = 0; i < 20; i++) {
            String close = String.valueOf(100 + i);
            candles.add(candle(baseDate.plusDays(i), close, String.valueOf(101 + i), String.valueOf(99 + i), close, 900));
        }

        TradeSignal signal = service.analyze(candles);

        assertThat(signal.signal()).isEqualTo(SignalType.HOLD);
        assertThat(signal.reasons()).contains("MA5 > MA10 > MA20 정배열입니다.");
    }

    @Test
    void analyze_returns_danger_when_price_falls_with_volume() {
        List<StockCandle> candles = new ArrayList<>();
        LocalDate baseDate = LocalDate.of(2026, 1, 1);

        for (int i = 0; i < 19; i++) {
            String close = String.valueOf(120 - i);
            candles.add(candle(baseDate.plusDays(i), close, String.valueOf(121 - i), String.valueOf(119 - i), close, 1000));
        }
        candles.add(candle(baseDate.plusDays(19), "101", "102", "97", "98", 2500));

        TradeSignal signal = service.analyze(candles);

        assertThat(signal.signal()).isEqualTo(SignalType.DANGER);
        assertThat(signal.reasons()).contains("가격 하락과 거래량 증가가 동시에 발생했습니다.");
    }

    @Test
    void analyze_returns_none_when_data_is_not_enough() {
        TradeSignal signal = service.analyze(List.of(
                candle(LocalDate.of(2026, 1, 1), "100", "101", "99", "100", 1000)
        ));

        assertThat(signal.signal()).isEqualTo(SignalType.NONE);
        assertThat(signal.reasons()).contains("20일 이동평균 계산에 필요한 데이터가 부족합니다.");
    }

    private StockCandle candle(LocalDate date, String open, String high, String low, String close, long volume) {
        return new StockCandle(
                date,
                new BigDecimal(open),
                new BigDecimal(high),
                new BigDecimal(low),
                new BigDecimal(close),
                volume
        );
    }
}
