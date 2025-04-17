package com.api.service.interfaces;

import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Class CardService
 *
 * Service interface for managing cards.
 * Provides methods for performing CRUD operations and other card-related actions.
 */
public interface CardService {

    /**
     * Retrieves a card by its ID.
     *
     * @param cardId The ID of the card.
     */
    CardDto getCardById(UUID cardId);

    /**
     * Adds a new card to the system.
     *
     * @param cardDtoNoId A `CardDtoNoId` object containing the details of the card to be added.
     *                    The card ID will be generated automatically.
     */
    CardDto addCard(CardDtoNoId cardDtoNoId);

    /**
     * Updates an existing card.
     *
     * @param cardDto A `CardDto` object containing the updated card details.
     */
    CardDto updateCard(CardDto cardDto);

    /**
     * Updates the status of a card.
     *
     * @param cardId The ID of the card to be updated.
     */
    void updateCardStatus(UUID cardId, String newStatus);

    /**
     * Updates the transaction limit per day for a card.
     *
     * @param cardId The ID of the card to be updated.
     * @param newLimit The new transaction limit per day.
     */
    void updateCardsTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit);

    /**
     * Deletes a card by its ID.
     *
     * @param cardId The ID of the card to be deleted.
     */
    void deleteCardById(UUID cardId);

    /**
     * Retrieves a paginated list of all cards.
     *
     * @param pageable The pagination information.
     */
    Page<CardDto> findAll(Pageable pageable);

    /**
     * Retrieves a paginated list of all cards owned by a specific user.
     *
     * @param ownerId The ID of the owner whose cards are to be retrieved.
     * @param pageable The pagination information.
     */
    Page<CardDto> findAllByOwnerId(UUID ownerId, Pageable pageable);
}
