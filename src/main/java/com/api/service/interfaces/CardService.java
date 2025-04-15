package com.api.service.interfaces;

import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface CardService {

    CardDto getCardById(UUID cardId);
    CardDto addCard(CardDtoNoId cardDtoNoId);
    CardDto updateCard(CardDto cardDto);
    void updateCardStatus(UUID cardId, String newStatus);
    void updateCardsTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit);
    void deleteCardById(UUID cardId);
    Page<CardDto> findAll(Pageable pageable);
    Page<CardDto> findAllByOwnerId(UUID ownerId, Pageable pageable);
}
