package com.thkim.toyproject.fintrack.application.api.transaction;

import com.thkim.toyproject.fintrack.application.api.transaction.dto.TransactionResponse;
import com.thkim.toyproject.fintrack.domain.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public Page<TransactionResponse> getTransactions(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = {"date", "id"}, // 기본 정렬 필드: date, id (쉼표로 구분)
                    direction = Sort.Direction.DESC // 기본 정렬 방향: 내림차순
            ) Pageable pageable
    ) {
        return transactionService.getTransactions(pageable);
    }
}