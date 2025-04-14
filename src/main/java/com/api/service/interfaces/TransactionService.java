package com.api.service.interfaces;

import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {

    TransactionDto addTransaction(TransactionDtoNoId transactionDtoNoId);
    TransactionDto updateTransaction(TransactionDto transactionDto);
    TransactionDto getTransactionById(UUID transactionId);
    void deleteTransactionById(UUID transactionId);
    void proceedPayment(Card sourceCardId, Card destinationCardId, BigDecimal amount);
    Page<TransactionDto> findAll(Pageable pageable);
    Page<TransactionDto> findAllByOwnerId(UUID ownerId, Pageable pageable);
}
