package com.api.service;

import com.api.config.EncryptionUtil;
import com.api.config.enums.CardStatus;
import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import com.api.entity.Card;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.service.interfaces.CardService;
import com.api.service.interfaces.TransactionService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;
    private final EncryptionUtil encryptionUtils;

    /**
     * @param cardId
     * @return
     */
    @Override
    public CardDto getCardById(UUID cardId) {
        return modelMapper.map(cardRepository.findById(cardId), CardDto.class);
    }

    /**
     * @param cardDtoNoId
     * @return
     */
    @Override
    public CardDto addCard(CardDtoNoId cardDtoNoId) {
        cardDtoNoId.setNumber(encryptionUtils.encrypt(cardDtoNoId.getNumber()));
        try{
            Card card = cardRepository.save(modelMapper.map(cardDtoNoId, Card.class));
            return modelMapper.map(card, CardDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("This element is already exists in the database");
        }
    }

    /**
     * @param cardDto
     * @return
     */
    @Override
    public CardDto updateCard(CardDto cardDto) {
        cardDto.setNumber(encryptionUtils.encrypt(cardDto.getNumber()));
        Card card = cardRepository.save(modelMapper.map(cardDto, Card.class));
        return modelMapper.map(card, CardDto.class);
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
     * @param cardId
     * @param newLimit
     * @return
     */
    @Transactional
    @Override
    public void updateCardsTransactionLimitPerDayById(UUID cardId, BigDecimal newLimit) {
        cardRepository.updateTransactionLimitPerDayById(cardId, newLimit); // отношения cards не существует??
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
     * @param ownerId
     * @return
     */
    @Override
    public Page<CardDto> findAllByOwnerId(UUID ownerId, Pageable pageable) {
        return cardRepository.findAllByOwnerId(ownerId, pageable)
                .map(card -> modelMapper.map(card, CardDto.class));
    }

}
