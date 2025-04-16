package com.api.config;

import com.api.config.enums.CardStatus;
import com.api.entity.Card;
import com.api.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * *") // per day at 00:00
    public void checkExpiredCards() {
        List<Card> expiredCards = cardRepository.findExpiredCards(LocalDateTime.now(), CardStatus.expired.name());
        for(Card card: expiredCards){
            card.setStatus(CardStatus.expired);
            log.info("expired card (id: {}, expired_date: {}, new status: {})",
                    card.getId(),card.getExpirationDate(), card.getStatus());
        }
        cardRepository.saveAll(expiredCards);
    }
}