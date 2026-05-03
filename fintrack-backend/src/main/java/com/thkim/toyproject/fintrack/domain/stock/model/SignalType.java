package com.thkim.toyproject.fintrack.domain.stock.model;

//  - BUY: 전일 눌림목 후보 + 당일 양봉 전환 + 전일 고가 돌파 + 거래량 증가
//  - HOLD: 정배열 상승 흐름 유지
//  - DANGER: 가격 하락 + 거래량 증가, 또는 역배열
//  - NONE: 명확한 신호 없음 / 데이터 부족
public enum SignalType {
    BUY,
    HOLD,
    DANGER,
    NONE
}
