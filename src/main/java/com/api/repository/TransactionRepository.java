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

//@Hidden
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findBySourceId(UUID cardId);
    Optional<Transaction> findByDestinationId(UUID cardId);
    boolean existsByIdAndSourceOwnerEmail(UUID id, String email);
    boolean existsByIdAndDestinationOwnerEmail(UUID id, String email);

    @Query(value = "SELECT * FROM \"bank-cards-management\".transactions " +
            "WHERE source_card_id = :cardId OR destination_card_id = :cardId",
            nativeQuery = true)
    Page<Transaction> findAllByCardId(@Param("cardId") UUID cardId, Pageable pageable);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM \"bank-cards-management\".transactions
            WHERE local_date_time >= :startDate
              AND local_date_time < :endDate
              AND source_card_id = :sourceCardId
        """, nativeQuery = true)
    BigDecimal getExpensesForSpecificSourceCardAndForSpecificDay(UUID sourceCardId, LocalDateTime startDate, LocalDateTime endDate);
}
