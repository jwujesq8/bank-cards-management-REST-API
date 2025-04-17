package com.api.service;

import com.api.config.EncryptionUtil;
import com.api.config.enums.CardStatus;
import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import com.api.entity.Card;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.service.interfaces.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Class CardServiceImpl
 *
 * Service implementation for managing cards.
 * Provides methods for adding, updating, deleting, and retrieving card information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;
    private final EncryptionUtil encryptionUtils;

    /**
     * Retrieves a card by its ID.
     *
     * @param cardId The ID of the card to be retrieved.
     */
    @Override
    public CardDto getCardById(UUID cardId) {
        return modelMapper.map(cardRepository.findById(cardId), CardDto.class);
    }

    /**
     * Adds a new card.
     *
     * @param cardDtoNoId The card details without the ID.
     * @throws BadRequestException if there is a data integrity violation while saving the card.
     */
    @Override
    public CardDto addCard(CardDtoNoId cardDtoNoId) {
        cardDtoNoId.setNumber(encryptionUtils.encrypt(cardDtoNoId.getNumber()));
        try{
            Card card = cardRepository.save(modelMapper.map(cardDtoNoId, Card.class));
            return modelMapper.map(card, CardDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Updates an existing card (if not expired).
     *
     * @param cardDto The updated card details.
     */
    @Override
    public CardDto updateCard(CardDto cardDto) {
        Card cardDb = cardRepository.findById(cardDto.getId()).orElseThrow(() ->
                new BadRequestException("There is no such card"));
        if(cardDb.getStatus().equals(CardStatus.expired) || cardDb.getStatus().equals(CardStatus.blocked)){
            throw new BadRequestException("The card can only be changed if the card has not expired or blocked status");
        }
        cardDto.setNumber(encryptionUtils.encrypt(cardDto.getNumber()));
        Card card = cardRepository.save(modelMapper.map(cardDto, Card.class));
        return modelMapper.map(card, CardDto.class);
    }

    /**
     * Updates the status of a card (if not expired).
     *
     * @param cardId    The ID of the card to update.
     * @param newStatus The new status to be set.
     */
    @Transactional
    @Override
    public void updateCardStatus(UUID cardId, String newStatus) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new BadRequestException("There is no such card"));
        // check if prev card status is not expired
        if (card.getStatus().equals(CardStatus.expired)){
            throw new BadRequestException("The card status can only be changed if the card has not expired");
        }
        cardRepository.updateStatus(cardId, newStatus);
    }

    /**
     * Updates the transaction limit per day for a card (if not expired or blocked).
     *
     * @param cardId     The ID of the card.
     * @param newLimit   The new transaction limit per day to be set.
     */
    @Transactional
    @Override
    public void updateCardsTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit) {
        Card cardDb = cardRepository.findById(cardId).orElseThrow(() ->
                new BadRequestException("There is no such card"));
        if(cardDb.getStatus().equals(CardStatus.expired) || cardDb.getStatus().equals(CardStatus.blocked)){
            throw new BadRequestException("The card can only be changed if the card has not expired or blocked status");
        }
        cardRepository.updateTransactionLimitPerDayById(cardId, newLimit);
    }

    /**
     * Deletes a card by its ID.
     * A card can only be deleted if there are no transactions where it is either the source or the destination.
     *
     * @param cardId The ID of the card to be deleted.
     */
    @Override
    public void deleteCardById(UUID cardId) {
        cardRepository.deleteById(cardId);
    }

    /**
     * Retrieves all cards with pagination.
     *
     * @param pageable Pagination information.
     */
    @Override
    public Page<CardDto> findAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(card -> modelMapper.map(card, CardDto.class));
    }

    /**
     * Retrieves all cards by the owner's ID with pagination.
     *
     * @param ownerId The ID of the card's owner.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link CardDto} objects representing the cards owned by the specified owner.
     */
    @Override
    public Page<CardDto> findAllByOwnerId(UUID ownerId, Pageable pageable) {
        return cardRepository.findAllByOwnerId(ownerId, pageable)
                .map(card -> modelMapper.map(card, CardDto.class));
    }

}
