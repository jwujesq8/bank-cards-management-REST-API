package com.api.service.validation;

import com.api.config.enums.CardStatus;
import com.api.entity.Card;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardValidator {

    private final CardRepository cardRepository;

    public Card getCardOrThrow(UUID cardId, String role) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new BadRequestException("There is no such " + role + " card")
        );
    }

    public Card getCardOrThrow_LockWrite(UUID cardId, String role) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new BadRequestException("There is no such " + role + " card")
        );
    }

    public Card getCardOrThrow(UUID cardId) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new BadRequestException("There is no such card")
        );
    }

    public Card getCardOrThrow_LockWrite(UUID cardId) {
        return cardRepository.findByIdLockWrite(cardId).orElseThrow(
                () -> new BadRequestException("There is no such card")
        );
    }

    public boolean doesCardStatusEqualTo(Card card, CardStatus cardStatus){
        return card.getStatus().equals(cardStatus);
    }
}