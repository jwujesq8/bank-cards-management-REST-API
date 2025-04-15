package com.api.repository;

import com.api.entity.Transaction;
import com.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

//@Hidden
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findBySourceId(UUID cardId);
    Optional<Transaction> findByDestinationId(UUID cardId);
}
