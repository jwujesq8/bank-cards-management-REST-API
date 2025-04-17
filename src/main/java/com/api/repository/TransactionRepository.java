package com.api.repository;

import com.api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Class TransactionRepository
 *
 * Repository interface for managing `Transaction` entities.
 * This interface extends `JpaRepository` and provides custom queries for transaction-related operations.
 *
 * It includes methods for retrieving transactions based on card IDs, checking the existence of transactions
 * by card ownership, and calculating specific expenses.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Finds a transaction where the source card matches the given card ID.
     *
     * @param cardId The ID of the source card.
     */
    Optional<Transaction> findBySourceId(UUID cardId);

    /**
     * Finds a transaction where the destination card matches the given card ID.
     *
     * @param cardId The ID of the destination card.
     */
    Optional<Transaction> findByDestinationId(UUID cardId);

    /**
     * Checks whether a transaction exists with the specified ID and the source card belonging to the given email.
     *
     * @param id The ID of the transaction.
     * @param email The email of the source card owner.
     */
    boolean existsByIdAndSourceOwnerEmail(UUID id, String email);


    /**
     * Checks whether a transaction exists with the specified ID and the destination card belonging to the given email.
     *
     * @param id The ID of the transaction.
     * @param email The email of the destination card owner.
     */
    boolean existsByIdAndDestinationOwnerEmail(UUID id, String email);

    /**
     * Retrieves all transactions where the specified card ID is either the source or destination card, paginated.
     *
     * @param cardId The ID of the card to search for in the source or destination.
     * @param pageable The pagination information.
     */
    @Query(value = "SELECT * FROM \"bank-cards-management\".transactions " +
            "WHERE source_card_id = :cardId OR destination_card_id = :cardId",
            nativeQuery = true)
    Page<Transaction> findAllByCardId(@Param("cardId") UUID cardId, Pageable pageable);

    /**
     * Calculates the total expenses for a specific source card within a given date range.
     *
     * @param sourceCardId The ID of the source card.
     * @param startDate The start date of the date range.
     * @param endDate The end date of the date range.
     */
    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM \"bank-cards-management\".transactions
            WHERE local_date_time >= :startDate
              AND local_date_time < :endDate
              AND source_card_id = :sourceCardId
        """, nativeQuery = true)
    BigDecimal getExpensesForSpecificSourceCardAndForSpecificDay(UUID sourceCardId, LocalDateTime startDate, LocalDateTime endDate);
}
