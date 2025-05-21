package com.api.service;

import com.api.service.validation.CardValidator;
import com.api.config.enums.CardStatus;
import com.api.config.enums.Role;
import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import com.api.dto.UserDto;
import com.api.entity.Card;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardServiceImplTest {

    private CardServiceImpl cardService;

    @Mock
    private CardRepository cardRepository;
    private ModelMapper modelMapper;
    @Mock
    private CardValidator cardValidator;

    public UUID cardId;
    public UUID userId;
    public CardDto userCardDto;
    public UserDto userDto;

    @BeforeEach
    void setUp(){
        modelMapper = new ModelMapper(); // basic ModelMapper for less coding but without ours ModelMapper config
        MockitoAnnotations.openMocks(this);
        cardService = new CardServiceImpl(cardRepository, cardValidator, modelMapper);
        cardId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userDto = UserDto.builder()
                .id(userId)
                .fullName("User Fullname")
                .email("user@gmail.com")
                .password("user_password123")
                .role(Role.USER)
                .build();
        userCardDto = CardDto.builder()
                .id(cardId)
                .number("1111-2222-3333-4444")
                .owner(userDto)
                .expirationDate(LocalDateTime.of(2026,12,31,00,00,00))
                .balance(BigDecimal.valueOf(500.00))
                .transactionLimitPerDay(BigDecimal.valueOf(1000.00))
                .status(CardStatus.active)
                .build();
    }

    @Nested
    class getCardById{

        @Test
        public void success(){
            when(cardValidator.getCardOrThrow(cardId)).thenReturn(modelMapper.map(userCardDto, Card.class));

            CardDto result = cardService.getCardById(cardId);

            assertNotNull(result);
            assertEquals(userCardDto.getId(), result.getId());
            assertEquals(userCardDto.getBalance(), result.getBalance());
            assertEquals(userCardDto.getNumber(), result.getNumber());
        }

        @Test
        public void nonexistentId_shouldThrowException(){
            when(cardValidator.getCardOrThrow(cardId))
                    .thenThrow(new BadRequestException("There is no such card"));

            assertThrows(BadRequestException.class, () -> cardService.getCardById(cardId));
        }
    }

    @Nested
    class addCard{

        @Test
        public void success(){
            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardRepository.save(any(Card.class))).thenReturn(card);

            CardDto result = cardService.addCard(modelMapper.map(userCardDto, CardDtoNoId.class));

            assertNotNull(result);
            assertEquals(userCardDto.getId(), result.getId());


        }

        @Test
        public void onDuplicate_shouldThrowBadRequestException(){
            when(cardRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

            assertThrows(BadRequestException.class, () -> cardService.addCard(modelMapper.map(userCardDto, CardDtoNoId.class)));
        }
    }

    @Nested
    class updateCard{

        @Test
        public void success() {
            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId)).thenReturn(card);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.active)).thenReturn(true);
            when(cardRepository.save(any(Card.class))).thenReturn(card);

            CardDto result = cardService.updateCard(userCardDto);

            assertNotNull(result);
            assertEquals(cardId, result.getId());
        }
        @Test
        public void nonexistentCard_shouldThrowException() {
            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId))
                    .thenThrow(new BadRequestException("There is no such card"));

            assertThrows(BadRequestException.class, () -> cardService.updateCard(userCardDto));
        }
        @Test
        public void inactiveCard_shouldThrowException() {
            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId)).thenReturn(card);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.active)).thenReturn(false);

            assertThrows(BadRequestException.class, () -> cardService.updateCard(userCardDto));
        }
    }

    @Nested
    class updateCardStatus{

        @Test
        public void success(){
            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId)).thenReturn(card);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.expired)).thenReturn(false);
            doNothing().when(cardRepository).updateStatus(any(UUID.class), any(String.class));

            cardService.updateCardStatus(cardId, "expired");

            verify(cardRepository).updateStatus(cardId, "expired");
        }
        @Test
        public void nonexistentCard_shouldThrowException(){
            when(cardValidator.getCardOrThrow_LockWrite(any(UUID.class)))
                    .thenThrow(new BadRequestException("There is no such card"));

            assertThrows(BadRequestException.class, () -> cardService.updateCardStatus(cardId, "expired"));
        }
        @Test
        public void cardStatusIsExpired_shouldThrowException(){
            Card card = modelMapper.map(userCardDto, Card.class);
            card.setStatus(CardStatus.expired);
            when(cardValidator.getCardOrThrow_LockWrite(cardId)).thenReturn(card);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.expired)).thenReturn(true);

            assertThrows(BadRequestException.class, () -> cardService.updateCardStatus(cardId, "active"));
        }
    }

    @Nested
    class updateCardsTransactionLimitPerDayById{

        @Test
        public void success(){
            BigDecimal newLimit = BigDecimal.valueOf(1000);

            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId)).thenReturn(card);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.expired)).thenReturn(false);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.blocked)).thenReturn(false);
            doNothing().when(cardRepository)
                    .updateTransactionLimitPerDayById(any(UUID.class), any(BigDecimal.class));

            cardService.updateCardsTransactionLimitPerDayById(cardId, newLimit);

            verify(cardRepository).updateTransactionLimitPerDayById(cardId, newLimit);
        }
        @Test
        public void nonexistentCard_shouldThrowException(){
            BigDecimal newLimit = BigDecimal.valueOf(1000);

            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId))
                    .thenThrow(new BadRequestException("There is no such card"));

            assertThrows(BadRequestException.class,
                    () -> cardService.updateCardsTransactionLimitPerDayById(cardId, newLimit));
        }
        @Test
        public void cardStatusIsExpired_shouldThrowException(){
            BigDecimal newLimit = BigDecimal.valueOf(1000);

            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId)).thenReturn(card);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.expired)).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> cardService.updateCardsTransactionLimitPerDayById(cardId, newLimit));
        }
        @Test
        public void cardStatusIsBlocked_shouldThrowException(){
            BigDecimal newLimit = BigDecimal.valueOf(1000);

            Card card = modelMapper.map(userCardDto, Card.class);
            when(cardValidator.getCardOrThrow_LockWrite(cardId)).thenReturn(card);
            when(cardValidator.isCardStatusEqualTo(card, CardStatus.blocked)).thenReturn(true);

            assertThrows(BadRequestException.class,
                    () -> cardService.updateCardsTransactionLimitPerDayById(cardId, newLimit));
        }
    }

    @Nested
    class deleteCardById{

        @Test
        public void shouldCallRepository(){
            cardService.deleteCardById(cardId);
            verify(cardRepository).deleteById(cardId);
        }
    }

    @Nested
    class findAll{

        @Test
        public void shouldReturnPageOfCardDto(){
            List<Card> cards = List.of(modelMapper.map(userCardDto, Card.class));
            Pageable pageable = PageRequest.of(0, 10);

            when(cardRepository.findAll(pageable)).thenReturn(new PageImpl<>(cards));

            assertEquals(1, cardService.findAll(pageable).getTotalElements());
        }
    }

    @Nested
    class findAllByOwnerId{

        @Test
        public void shouldReturnPageOfCardDto(){
            Pageable pageable = PageRequest.of(0, 10);

            when(cardRepository.findAllByOwnerId(userId, pageable))
                    .thenReturn(new PageImpl<>(List.of(modelMapper.map(userCardDto, Card.class))));

            assertEquals(1, cardService.findAllByOwnerId(userId, pageable).getTotalElements());
        }
    }
}