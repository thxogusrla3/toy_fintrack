package com.thkim.toyproject.fintrack.domain.stock;

import com.thkim.toyproject.fintrack.domain.stock.model.SignalType;
import com.thkim.toyproject.fintrack.domain.stock.model.StockCandle;
import com.thkim.toyproject.fintrack.domain.stock.model.TradeSignal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StockSignalService {
    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal NEAR_MA_RATE = new BigDecimal("0.02");
    private static final BigDecimal VOLUME_INCREASE_RATE = new BigDecimal("1.5");

    public TradeSignal analyze(List<StockCandle> candles) {
        if (candles == null || candles.size() < 20) {
            return new TradeSignal(SignalType.NONE, List.of("20일 이동평균 계산에 필요한 데이터가 부족합니다."));
        }

        List<StockCandle> sortedCandles = candles.stream()
                .sorted(Comparator.comparing(StockCandle::date))
                .toList();

        StockCandle current = sortedCandles.get(sortedCandles.size() - 1);
        StockCandle previous = sortedCandles.get(sortedCandles.size() - 2);
        BigDecimal ma5 = movingAverage(sortedCandles, sortedCandles.size() - 1, 5);
        BigDecimal ma10 = movingAverage(sortedCandles, sortedCandles.size() - 1, 10);
        BigDecimal ma20 = movingAverage(sortedCandles, sortedCandles.size() - 1, 20);
        BigDecimal volumeMa5 = volumeAverage(sortedCandles, sortedCandles.size() - 1, 5);

        boolean uptrend = isUptrend(ma5, ma10, ma20) && current.close().compareTo(ma5) > 0;
        boolean downtrend = ma5.compareTo(ma10) < 0 && ma10.compareTo(ma20) < 0;
        boolean priceRises = current.close().compareTo(previous.close()) > 0;
        boolean priceFalls = current.close().compareTo(previous.close()) < 0;
        boolean volumeIncreases = BigDecimal.valueOf(current.volume()).compareTo(volumeMa5.multiply(VOLUME_INCREASE_RATE, MC)) > 0;
        boolean volumeDecreases = BigDecimal.valueOf(current.volume()).compareTo(volumeMa5) < 0;

        List<String> reasons = new ArrayList<>();

        if (priceFalls && volumeIncreases) {
            reasons.add("가격 하락과 거래량 증가가 동시에 발생했습니다.");
            reasons.add("하락 강화 위험 구간입니다.");
            return new TradeSignal(SignalType.DANGER, reasons);
        }

        if (isBuySignal(sortedCandles, current, previous)) {
            reasons.add("전일 눌림목 후보 이후 양봉 전환이 발생했습니다.");
            reasons.add("전일 고가를 돌파했습니다.");
            reasons.add("거래량이 최근 5일 평균 대비 1.5배 이상 증가했습니다.");
            return new TradeSignal(SignalType.BUY, reasons);
        }

        if (uptrend && priceRises) {
            reasons.add("MA5 > MA10 > MA20 정배열입니다.");
            reasons.add(volumeDecreases ? "상승 중 거래량이 감소해 보유 관점입니다." : "상승 흐름이 유지되고 있습니다.");
            return new TradeSignal(SignalType.HOLD, reasons);
        }

        if (downtrend) {
            reasons.add("MA5 < MA10 < MA20 역배열입니다.");
            return new TradeSignal(SignalType.DANGER, reasons);
        }

        reasons.add("명확한 매수 또는 위험 신호가 없습니다.");
        return new TradeSignal(SignalType.NONE, reasons);
    }

    private boolean isBuySignal(List<StockCandle> candles, StockCandle current, StockCandle previous) {
        if (candles.size() < 21) {
            return false;
        }

        int previousIndex = candles.size() - 2;
        BigDecimal previousMa5 = movingAverage(candles, previousIndex, 5);
        BigDecimal previousMa10 = movingAverage(candles, previousIndex, 10);
        BigDecimal previousMa20 = movingAverage(candles, previousIndex, 20);
        BigDecimal previousVolumeMa5 = volumeAverage(candles, previousIndex - 1, 5);
        BigDecimal currentVolumeMa5 = volumeAverage(candles, candles.size() - 2, 5);

        boolean previousUptrend = isUptrend(previousMa5, previousMa10, previousMa20);
        boolean previousNearMa = isNear(previous.close(), previousMa5) || isNear(previous.close(), previousMa10);
        boolean previousVolumeDecreased = BigDecimal.valueOf(previous.volume()).compareTo(previousVolumeMa5) < 0;
        boolean bullishCandle = current.close().compareTo(current.open()) > 0;
        boolean breaksPreviousHigh = current.close().compareTo(previous.high()) > 0;
        boolean volumeIncreased = BigDecimal.valueOf(current.volume()).compareTo(currentVolumeMa5.multiply(VOLUME_INCREASE_RATE, MC)) > 0;

        return previousUptrend
                && previousNearMa
                && previousVolumeDecreased
                && bullishCandle
                && breaksPreviousHigh
                && volumeIncreased;
    }

    private boolean isUptrend(BigDecimal ma5, BigDecimal ma10, BigDecimal ma20) {
        return ma5.compareTo(ma10) > 0 && ma10.compareTo(ma20) > 0;
    }

    private boolean isNear(BigDecimal price, BigDecimal movingAverage) {
        BigDecimal gap = price.subtract(movingAverage).abs();
        BigDecimal rate = gap.divide(movingAverage, MC);
        return rate.compareTo(NEAR_MA_RATE) <= 0;
    }

    private BigDecimal movingAverage(List<StockCandle> candles, int endIndex, int days) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = endIndex - days + 1; i <= endIndex; i++) {
            sum = sum.add(candles.get(i).close());
        }
        return sum.divide(BigDecimal.valueOf(days), MC);
    }

    private BigDecimal volumeAverage(List<StockCandle> candles, int endIndex, int days) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = endIndex - days + 1; i <= endIndex; i++) {
            sum = sum.add(BigDecimal.valueOf(candles.get(i).volume()));
        }
        return sum.divide(BigDecimal.valueOf(days), MC);
    }

}
