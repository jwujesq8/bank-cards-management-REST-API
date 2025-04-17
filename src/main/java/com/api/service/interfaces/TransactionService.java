package com.api.service.interfaces;

import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Class TransactionService
 *
 * Service interface for managing transactions.
 * Provides methods for performing CRUD operations and executing transactions between cards.
 */
public interface TransactionService {

    /**
     * Retrieves a transaction by its ID.
     *
     * @param transactionId The ID of the transaction.
     */
    TransactionDto getTransactionById(UUID transactionId);

    /**
     * Adds a new transaction to the system.
     *
     * @param transactionDtoNoId A `TransactionDtoNoId` object containing the details of the transaction to be added.
     *                           The transaction ID will be generated automatically.
     */
    TransactionDto addTransaction(TransactionDtoNoId transactionDtoNoId);

    /**
     * Updates an existing transaction.
     *
     * @param transactionDto A `TransactionDto` object containing the updated transaction details.
     */
    TransactionDto updateTransaction(TransactionDto transactionDto);

    /**
     * Deletes a transaction by its ID.
     *
     * @param transactionId The ID of the transaction to be deleted.
     */
    void deleteTransactionById(UUID transactionId);

    /**
     * Retrieves a paginated list of all transactions.
     *
     * @param pageable The pagination information.
     */
    Page<TransactionDto> findAll(Pageable pageable);

    /**
     * Performs a transaction between two cards.
     *
     * @param sourceCardId The ID of the source card.
     * @param destinationCardId The ID of the destination card.
     * @param amount The amount to be transferred between the cards.
     */
    void makeTransaction(UUID sourceCardId, UUID destinationCardId, BigDecimal amount);

    /**
     * Retrieves a paginated list of all transactions associated with a specific card.
     *
     * @param cardId The ID of the card for which transactions are to be retrieved.
     * @param pageable The pagination information.
     */
    Page<TransactionDto> findAllByCardId(UUID cardId, Pageable pageable);

}
