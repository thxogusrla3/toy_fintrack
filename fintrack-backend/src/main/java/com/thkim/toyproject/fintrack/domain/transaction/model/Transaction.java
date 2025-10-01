package com.thkim.toyproject.fintrack.domain.transaction.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name ="transactions",  indexes = {
        @Index(name = "ix_tx_user_cat_date", columnList ="user_id, category, date"),
        @Index(name = "ix_tx_user_type_date", columnList = "user_id, type, date")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name="type", nullable = false)
    private String type;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "memo")
    private String memo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
