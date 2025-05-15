package com.api.repository;

import com.api.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Class CardRepository
 *
 * Repository interface for managing `Card` entities.
 * This interface extends `JpaRepository` and provides custom queries for card-related operations.
 *
 * It includes methods for retrieving cards by various criteria, updating card properties,
 * and checking the existence of cards by specific conditions.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdLockWrite(UUID id);

    /**
     * Finds a card by its number.
     *
     * @param number The card number.
     */
    Optional<Card> findByNumber(String number);

    /**
     * Retrieves all cards belonging to a specific owner, paginated.
     *
     * @param ownerId The ID of the owner.
     * @param pageable The pagination information.
     */
    Page<Card> findAllByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * Checks whether a card with the specified ID exists and belongs to the given owner email.
     *
     * @param cardId The ID of the card.
     * @param email The email of the card owner.
     */
    boolean existsByIdAndOwnerEmail(UUID cardId, String email);

    /**
     * Updates the transaction limit per day for a card by its ID.
     *
     * @param cardId The ID of the card to update.
     * @param newLimit The new transaction limit per day.
     */
    @Modifying
    @Query(value = "UPDATE \"bank_cards_management\".cards SET transaction_limit_per_day = :newLimit WHERE id = :cardId",
        nativeQuery = true)
    void updateTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit);


    /**
     * Updates the status of a card by its ID.
     *
     * @param cardId The ID of the card to update.
     * @param newStatus The new status of the card.
     */
    @Modifying
    @Query(value = "UPDATE \"bank_cards_management\".cards SET status = :newStatus WHERE id = :cardId",
            nativeQuery = true)
    void updateStatus(UUID cardId, String newStatus);

    /**
     * Finds cards that have expired before a specified date and have not been marked as expired.
     *
     * @param date The date to compare against.
     * @param expiredStatus The status that indicates a card is expired.
     */
    @Query(value = """
            SELECT * FROM \"bank_cards_management\".cards
            WHERE expiration_date < :date
              AND status != :expiredStatus
            """,
        nativeQuery = true)
    List<Card> findExpiredCards(LocalDateTime date, String expiredStatus);

}
