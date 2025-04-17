package com.api.service;

import com.api.config.EncryptionUtil;
import com.api.config.enums.CardStatus;
import com.api.config.enums.Role;
import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import com.api.dto.UserDto;
import com.api.entity.Card;
import com.api.entity.User;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardServiceImplTest {

    private CardServiceImpl cardService;

    @Mock
    private CardRepository cardRepository;
    private ModelMapper modelMapper;
    @Mock
    private EncryptionUtil encryptionUtils;

    public UUID cardId;
    public UUID userId;
    public CardDto userCardDto;
    public CardDtoNoId userCardDtoNoId;
    public Card userCard;
    public UserDto userDto;
    public User user;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        modelMapper = new ModelMapper();
        cardService = new CardServiceImpl(cardRepository, modelMapper, encryptionUtils);
        cardId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userDto = UserDto.builder()
                .id(userId)
                .fullName("User Fullname")
                .email("user@gmail.com")
                .password("user_password123")
                .role(Role.USER)
                .build();
        user = User.builder()
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
        userCardDtoNoId = CardDtoNoId.builder()
                .number("1111-2222-3333-4444")
                .owner(userDto)
                .expirationDate(LocalDateTime.of(2026,12,31,00,00,00))
                .balance(BigDecimal.valueOf(500.00))
                .transactionLimitPerDay(BigDecimal.valueOf(1000.00))
                .status(CardStatus.active)
                .build();
        userCard = Card.builder()
                .id(cardId)
                .number("1111-2222-3333-4444")
                .owner(user)
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
            when(cardRepository.findById(cardId)).thenReturn(Optional.ofNullable(userCard));

            CardDto result = cardService.getCardById(cardId);

            assertNotNull(result);
            assertEquals(userCardDto.getId(), result.getId());
            assertEquals(userCardDto.getBalance(), result.getBalance());
            assertEquals(userCardDto.getNumber(), result.getNumber());
            verify(cardRepository).findById(cardId);

        }
    }

    @Nested
    class addCard{

        @Test
        public void shouldEncryptAndSaveCard(){
            when(encryptionUtils.encrypt(anyString())).thenReturn("encrypted");
            when(cardRepository.save(any())).thenReturn(userCard);

            CardDto result = cardService.addCard(userCardDtoNoId);

            assertNotNull(result);
            assertEquals(userCardDto.getNumber(), result.getNumber());
            assertEquals(userCardDto.getOwner().getId(), result.getOwner().getId());
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        public void shouldThrowBadRequestException_onDuplicate(){
            when(encryptionUtils.encrypt(anyString())).thenReturn("encrypted");
            when(cardRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

            assertThrows(BadRequestException.class, () -> cardService.addCard(userCardDtoNoId));
        }
    }

    @Nested
    class updateCard{

        @Test
        public void shouldEncryptAndUpdateCard() {
            when(encryptionUtils.encrypt(anyString())).thenReturn("encrypted");
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(userCard));
            when(cardRepository.save(any())).thenReturn(userCard);

            CardDto result = cardService.updateCard(userCardDto);

            assertNotNull(result);
            assertEquals(cardId, result.getId());
        }
    }

    @Nested
    class updateCardStatus{

        @Test
        public void shouldCallRepository(){
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(userCard));
            cardService.updateCardStatus(cardId, "expired");
            verify(cardRepository).updateStatus(cardId, "expired");
        }
    }

    @Nested
    class updateCardsTransactionLimitPerDayById{

        @Test
        public void shouldCallRepository(){
            BigDecimal newLimit = BigDecimal.valueOf(1000);
            when(cardRepository.findById(cardId)).thenReturn(Optional.of(userCard));

            cardService.updateCardsTransactionLimitPerDayById(cardId, newLimit);

            verify(cardRepository).updateTransactionLimitPerDayById(cardId, newLimit);
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
            List<Card> cards = List.of(userCard);
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
                    .thenReturn(new PageImpl<>(List.of(userCard)));

            assertEquals(1, cardService.findAllByOwnerId(userId, pageable).getTotalElements());
        }
    }
}