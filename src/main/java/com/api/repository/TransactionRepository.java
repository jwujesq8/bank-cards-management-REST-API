package com.api.repository;

import com.api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

//@Hidden
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findBySourceId(UUID cardId);
    Optional<Transaction> findByDestinationId(UUID cardId);

    @Query(value = "SELECT * FROM \"bank-cards-management\".transactions " +
            "WHERE source_card_id = :cardId OR destination_card_id = :cardId",
            nativeQuery = true)
    Page<Transaction> findAllByCardId(@Param("cardId") UUID cardId, Pageable pageable);
}
