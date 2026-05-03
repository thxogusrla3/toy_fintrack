package com.thkim.toyproject.fintrack.domain.stock.model;

import java.util.List;

public record TradeSignal(
        SignalType signal,
        List<String> reasons
) {
}
