package com.api.service.interfaces;

import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {

    TransactionDto getTransactionById(UUID transactionId);
    TransactionDto addTransaction(TransactionDtoNoId transactionDtoNoId);
    TransactionDto updateTransaction(TransactionDto transactionDto);
    void deleteTransactionById(UUID transactionId);
    Page<TransactionDto> findAll(Pageable pageable);
    void makeTransaction(UUID sourceCardId, UUID destinationCardId, BigDecimal amount);

}
