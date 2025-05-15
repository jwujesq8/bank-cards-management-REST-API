package com.api.service.executor;

import com.api.entity.Card;
import com.api.entity.Transaction;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import com.api.service.executor.interfaces.InternalTransactionExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InternalTransactionExecutorImpl implements InternalTransactionExecutor {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    /**
     *
     */
    @Override
    public void performTransaction(Card sourceCard, Card destinationCard, BigDecimal amount) {
        // Plus amount to the destination and minus from the source
        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destinationCard.setBalance(destinationCard.getBalance().add(amount));

        // Update cards with new balances
        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);

        // Add transaction to the database
        Transaction transaction = new Transaction(
                sourceCard, destinationCard, LocalDateTime.now(), amount
        );
        transactionRepository.save(transaction);
    }
}
