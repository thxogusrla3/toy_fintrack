package com.thkim.toyproject.fintrack.application.api.transaction.dto;

import com.thkim.toyproject.fintrack.domain.transaction.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String userId;
    private String type;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
    private String memo;
    private LocalDateTime createdAt;

    //정적 팩토리 메소드
    public static TransactionResponse fromEntity(Transaction tr) {
        return new TransactionResponse(
                tr.getId(),
                tr.getUserId(),
                tr.getType(),
                tr.getCategory(),
                tr.getAmount(),
                tr.getDate(),
                tr.getMemo(),
                tr.getCreatedAt()
        );
    }
}
