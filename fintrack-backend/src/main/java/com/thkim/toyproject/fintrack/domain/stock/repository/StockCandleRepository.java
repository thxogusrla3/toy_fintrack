package com.thkim.toyproject.fintrack.domain.stock.repository;

import com.thkim.toyproject.fintrack.domain.stock.model.StockCandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockCandleRepository extends JpaRepository<StockCandleEntity, Long> {
    Optional<StockCandleEntity> findByStockCodeAndDate(String stockCode, LocalDate date);

    List<StockCandleEntity> findTop60ByStockCodeOrderByDateDesc(String stockCode);

    long countByStockCode(String stockCode);
}
