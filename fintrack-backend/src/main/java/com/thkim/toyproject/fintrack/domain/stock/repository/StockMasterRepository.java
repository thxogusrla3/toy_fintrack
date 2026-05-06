package com.thkim.toyproject.fintrack.domain.stock.repository;

import com.thkim.toyproject.fintrack.domain.stock.model.StockMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockMasterRepository extends JpaRepository<StockMasterEntity, Long> {
    Optional<StockMasterEntity> findByStockCode(String stockCode);

    List<StockMasterEntity> findByThemeKey(String themeKey);
}
