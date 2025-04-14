package com.api.service.interfaces;

import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    CardDto addCard(CardDtoNoId cardDtoNoId);
    CardDto updateCard(CardDto cardDto);
    CardDto getCardById(UUID cardId);
    void deleteCardById(UUID cardId);
    Page<CardDto> findAll(Pageable pageable);
    void updateCardsTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit);
    Page<CardDto> findAllByOwnerId(UUID ownerId, Pageable pageable);
    void updateCardStatus(UUID cardId, String newStatus);
    void makePayment(UUID sourceCardId, UUID destinationCardId, BigDecimal amount);
}
