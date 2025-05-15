package com.api.service.validation;

import com.api.config.enums.CardStatus;
import com.api.entity.Card;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionValidator {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @AllArgsConstructor
    @Getter
    public static class SourceAndDestinationCards{
        private Card source;
        private Card destination;
    }

    public Card getCardOrThrow(UUID cardId, String role) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new BadRequestException("There is no such " + role + " card")
        );
    }

    public void validateCardStatus(Card card, String role) {
        if (!CardStatus.active.equals(card.getStatus())) {
            throw new BadRequestException(role + " card is not active or expired");
        }
    }

    public void validateDifferentCards(Card source, Card destination) {
        if (source.getId().equals(destination.getId())) {
            throw new BadRequestException("Source and destination card must be different");
        }
    }

    public void validateSameOwner(Card source, Card destination) {
        if (!source.getOwner().getId().equals(destination.getOwner().getId())) {
            throw new BadRequestException("Only same-owner transactions are allowed");
        }
    }

    public void validateSufficientFunds(Card source, BigDecimal amount) {
        if (source.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }
    }

    public void validateDailyLimit(Card source, BigDecimal amount) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        BigDecimal expensesForToday = transactionRepository.getExpensesForSpecificSourceCardAndForSpecificDay(
                source.getId(), start, end);
        if (source.getTransactionLimitPerDay().compareTo(expensesForToday.add(amount)) < 0) {
            throw new BadRequestException("Daily limit exceeded: " + source.getTransactionLimitPerDay());
        }
    }

    public SourceAndDestinationCards makeTransaction_validateCardsAndAmount(
            UUID sourceCardId,
            UUID destinationCardId,
            BigDecimal amount) {
        Card sourceCard = getCardOrThrow(sourceCardId, "source");
        Card destinationCard = getCardOrThrow(destinationCardId, "destination");

        validateCardStatus(sourceCard, "source");
        validateCardStatus(destinationCard, "destination");
        validateDifferentCards(sourceCard, destinationCard);
        validateSameOwner(sourceCard, destinationCard);
        validateSufficientFunds(sourceCard, amount);
        validateDailyLimit(sourceCard, amount);
        return new SourceAndDestinationCards(sourceCard, destinationCard);
    }
}
