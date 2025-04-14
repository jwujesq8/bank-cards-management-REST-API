package com.api.service;

import com.api.config.enums.CardStatus;
import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import com.api.entity.Card;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.service.interfaces.CardService;
import com.api.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;
    private final TransactionService transactionService;


    /**
     * @param cardDtoNoId
     * @return
     */
    @Override
    public CardDto addCard(CardDtoNoId cardDtoNoId) {
        Card card = cardRepository.save(modelMapper.map(cardDtoNoId, Card.class));
        return modelMapper.map(card, CardDto.class);
    }

    /**
     * @param cardDto
     * @return
     */
    @Override
    public CardDto updateCard(CardDto cardDto) {
        Card card = cardRepository.save(modelMapper.map(cardDto, Card.class));
        return modelMapper.map(card, CardDto.class);
    }

    /**
     * @param cardId
     * @return
     */
    @Override
    public CardDto getCardById(UUID cardId) {
        return modelMapper.map(cardRepository.findById(cardId), CardDto.class);
    }

    /**
     * @param cardId
     */
    @Override
    public void deleteCardById(UUID cardId) {
        cardRepository.deleteById(cardId);
    }

    /**
     * @param pageable
     * @return
     */
    @Override
    public Page<CardDto> findAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(card -> modelMapper.map(card, CardDto.class));
    }

    /**
     * @param cardId
     * @param newLimit
     * @return
     */
    @Transactional
    @Override
    public void updateCardsTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit) {
        cardRepository.updateTransactionLimitPerDayById(cardId, newLimit);
    }

    /**
     * @param ownerId
     * @return
     */
    @Override
    public Page<CardDto> findAllByOwnerId(UUID ownerId, Pageable pageable) {
        return cardRepository.findAllByOwnerId(ownerId, pageable)
                .map(card -> modelMapper.map(card, CardDto.class));
    }

    /**
     * @param cardId
     * @param newStatus
     * @return
     */
    @Transactional
    @Override
    public void updateCardStatus(UUID cardId, String newStatus) {
        cardRepository.updateStatus(cardId, newStatus);
    }

    /**
     * @param sourceCardId
     * @param destinationCardId
     * @param amount
     */
    @Transactional
    @Override
    public void makePayment(UUID sourceCardId, UUID destinationCardId, BigDecimal amount) {
        Card sourceCard = cardRepository.findById(sourceCardId).orElseThrow(
                () -> new BadRequestException("There is no such source card")
        );
        Card destinationCard = cardRepository.findById(destinationCardId).orElseThrow(
                () -> new BadRequestException("There is no such destination card")
        );
        if (!CardStatus.active.equals(sourceCard.getStatus())) {
            throw new BadRequestException("Source card is not active or expired");
        }
        if (!CardStatus.active.equals(destinationCard.getStatus())) {
            throw new BadRequestException("Destination card is not active or expired");
        }
        if (sourceCard.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }

        // Plus amount to the destination and minus from the source
        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destinationCard.setBalance(destinationCard.getBalance().add(amount));

        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);

        // Add transactions
        transactionService.proceedPayment(sourceCard, destinationCard, amount);

    }
}
