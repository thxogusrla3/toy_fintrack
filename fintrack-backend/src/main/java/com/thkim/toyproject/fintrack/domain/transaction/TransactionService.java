package com.thkim.toyproject.fintrack.domain.transaction;

import com.thkim.toyproject.fintrack.application.api.transaction.dto.TransactionResponse;
import com.thkim.toyproject.fintrack.domain.transaction.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;

    public Page<TransactionResponse> getTransactions(Pageable pageable) {
        return repository.findAll(pageable)
                .map(TransactionResponse::fromEntity);
    }
}
