package com.api.repository;


import com.api.config.enums.CardStatus;
import com.api.entity.Card;
import com.api.entity.User;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByNumber(String number);
    Page<Card> findAllByOwnerId(UUID ownerId, Pageable pageable);
    boolean existsByIdAndOwnerEmail(UUID cardId, String email);

    @Modifying
    @Query(value = "UPDATE \"bank-cards-management\".cards SET transaction_limit_per_day = :newLimit WHERE id = :cardId",
        nativeQuery = true)
    void updateTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit);

    @Modifying
    @Query(value = "UPDATE \"bank-cards-management\".cards SET status = :newStatus WHERE id = :cardId",
            nativeQuery = true)
    void updateStatus(UUID cardId, String newStatus);

    @Query(value = """
            SELECT * FROM \"bank-cards-management\".cards
            WHERE expiration_date < :date
              AND status != :expiredStatus
            """,
        nativeQuery = true)
    List<Card> findExpiredCards(LocalDateTime date, String expiredStatus);

}
