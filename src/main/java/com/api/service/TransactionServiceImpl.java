package com.api.service;

import com.api.dto.CardDto;
import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.entity.Card;
import com.api.entity.Transaction;
import com.api.repository.TransactionRepository;
import com.api.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;

    /**
     * @param transactionDtoNoId
     * @return
     */
    @Override
    public TransactionDto addTransaction(TransactionDtoNoId transactionDtoNoId) {
        Transaction transaction = transactionRepository.save(modelMapper.map(transactionDtoNoId, Transaction.class));
        return modelMapper.map(transaction, TransactionDto.class);
    }

    /**
     * @param transactionDto
     * @return
     */
    @Override
    public TransactionDto updateTransaction(TransactionDto transactionDto) {
        Transaction transaction = transactionRepository.save(modelMapper.map(transactionDto, Transaction.class));
        return modelMapper.map(transaction, TransactionDto.class);
    }

    /**
     * @param transactionId
     * @return
     */
    @Override
    public TransactionDto getTransactionById(UUID transactionId) {
        return modelMapper.map(transactionRepository.findById(transactionId), TransactionDto.class);
    }

    /**
     * @param transactionId
     */
    @Override
    public void deleteTransactionById(UUID transactionId) {
        transactionRepository.deleteById(transactionId);
    }

    /**
     * @param sourceCardId
     * @param destinationCardId
     * @param amount
     */
    @Transactional
    public void proceedPayment(Card sourceCardId, Card destinationCardId, BigDecimal amount){
        Transaction transaction = new Transaction(
                sourceCardId, destinationCardId, LocalDateTime.now(), amount
        );
        transactionRepository.save(transaction);
    };

    /**
     * @param pageable
     * @return
     */
    @Override
    public Page<TransactionDto> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(
                transaction -> modelMapper.map(transaction, TransactionDto.class));
    }

    /**
     * @param ownerId
     * @return
     */
    @Override
    public Page<TransactionDto> findAllByOwnerId(UUID ownerId, Pageable pageable) {
        return transactionRepository.findAllByOwnerId(ownerId, pageable).map(
                transaction -> modelMapper.map(transaction, TransactionDto.class));
    }
}
