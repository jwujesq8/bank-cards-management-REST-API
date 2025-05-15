package com.api.service;

import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.entity.Transaction;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import com.api.service.executor.interfaces.InternalTransactionExecutor;
import com.api.service.interfaces.TransactionService;
import com.api.service.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Class TransactionServiceImpl
 *
 * Service implementation for managing transactions.
 * Provides methods for adding, updating, deleting, and retrieving transaction information,
 * as well as handling the logic for making transactions between cards.
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;
    private final InternalTransactionExecutor internalTransactionExecutor;
    private final TransactionValidator transactionValidator;

    /**
     * Retrieves a transaction by its ID.
     *
     * @param transactionId The ID of the transaction to be retrieved.
     */
    @Override
    public TransactionDto getTransactionById(UUID transactionId) {
        return modelMapper.map(transactionRepository.findById(transactionId), TransactionDto.class);
    }

    /**
     * Adds a new transaction.
     *
     * @param transactionDtoNoId The transaction details without the ID.
     */
    @Override
    public TransactionDto addTransaction(TransactionDtoNoId transactionDtoNoId) {
        Transaction transaction = transactionRepository.save(modelMapper.map(transactionDtoNoId, Transaction.class));
        return modelMapper.map(transaction, TransactionDto.class);
    }

    /**
     * Updates an existing transaction.
     *
     * @param transactionDto The updated transaction details.
     */
    @Override
    public TransactionDto updateTransaction(TransactionDto transactionDto) {
        Transaction transaction = transactionRepository.save(modelMapper.map(transactionDto, Transaction.class));
        return modelMapper.map(transaction, TransactionDto.class);
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param transactionId The ID of the transaction to be deleted.
     */
    @Override
    public void deleteTransactionById(UUID transactionId) {
        transactionRepository.deleteById(transactionId);
    }

    /**
     * Retrieves all transactions with pagination.
     *
     * @param pageable Pagination information.
     */
    @Override
    public Page<TransactionDto> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(
                transaction -> modelMapper.map(transaction, TransactionDto.class));
    }

    /**
     * Handles the logic of making a transaction between two cards.
     * The method ensures that the source card has sufficient balance,
     * checks the transaction limits, and updates the balances of both cards accordingly.
     *
     * @param sourceCardId      The ID of the source card.
     * @param destinationCardId The ID of the destination card.
     * @param amount            The amount to be transferred.
     * @throws BadRequestException If any validation check fails.
     */
    @Override
    public void makeTransaction(UUID sourceCardId, UUID destinationCardId, BigDecimal amount){
        // Validation to make transaction (different cards, same owner, day limit etc.)
        TransactionValidator.SourceAndDestinationCards sourceAndDestinationCards =
                transactionValidator.makeTransaction_validateCardsAndAmount(sourceCardId, destinationCardId, amount);
        // Perform transaction (set new balances and save to DB)
        internalTransactionExecutor.performTransaction(
                sourceAndDestinationCards.getSource(),
                sourceAndDestinationCards.getDestination(),
                amount);
    }

    /**
     * Retrieves all transactions for a specific card with pagination.
     *
     * @param cardId  The ID of the card to retrieve transactions for.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link TransactionDto} objects representing the transactions for the specified card.
     */
    @Override
    public Page<TransactionDto> findAllByCardId(UUID cardId, Pageable pageable){
        return transactionRepository.findAllByCardId(cardId, pageable)
                .map(transaction -> modelMapper.map(transaction, TransactionDto.class));
    }
}
