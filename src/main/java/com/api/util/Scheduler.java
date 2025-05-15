package com.api.util;

import com.api.config.enums.CardStatus;
import com.api.entity.Card;
import com.api.repository.CardRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Class Scheduler
 *
 * Scheduled task that checks for expired cards and updates their status.
 * This component runs daily at midnight and marks cards as expired if their expiration date has passed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final CardRepository cardRepository;

    /**
     * Scheduled method that runs every day at 00:00.
     * Finds cards with expiration dates in the past and updates their status to {@code expired}.
     * Logs each updated card and saves all changes to the database.
     */
    @Scheduled(cron = "0 0 0 * * *") // per day at 00:00
    public void checkExpiredCards() {
        log.info("running expired cards check...");
        List<Card> expiredCards = cardRepository.findExpiredCards(LocalDateTime.now(), CardStatus.expired.name());
        for(Card card: expiredCards){
            card.setStatus(CardStatus.expired);
            log.info("expired card (id: {}, expired_date: {}, new status: {})",
                    card.getId(),card.getExpirationDate(), card.getStatus());
        }
        cardRepository.saveAll(expiredCards);
        log.info("expired cards check is finished...");
    }
}