package com.api.service;

import com.api.config.enums.CardStatus;
import com.api.config.enums.Role;
import com.api.dto.CardDto;
import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.dto.UserDto;
import com.api.entity.Card;
import com.api.entity.Transaction;
import com.api.entity.User;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardRepository cardRepository;
    private ModelMapper modelMapper;
    TransactionServiceImpl transactionService;
    private UUID transactionId;
    private TransactionDtoNoId transactionDtoNoId;
    private TransactionDto transactionDto;
    private Transaction transaction;
    private UUID sourceCardId;
    private UUID destinationCardId;
    private UUID userId;
    private CardDto sourceCardDto;
    private CardDto destinationCardDto;
    private UserDto userDto;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        modelMapper = new ModelMapper();
        transactionService = new TransactionServiceImpl(transactionRepository, cardRepository, modelMapper);
        transactionId = UUID.randomUUID();
        sourceCardId = UUID.randomUUID();
        destinationCardId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userDto = UserDto.builder()
                .id(userId)
                .fullName("User Fullname")
                .email("user@gmail.com")
                .password("user_password123")
                .role(Role.USER)
                .build();
        sourceCardDto = CardDto.builder()
                .id(sourceCardId)
                .number("1111-2222-3333-4444")
                .owner(userDto)
                .expirationDate(LocalDateTime.of(2026,12,31,00,00,00))
                .balance(BigDecimal.valueOf(1000.00))
                .transactionLimitPerDay(BigDecimal.valueOf(1000.00))
                .status(CardStatus.active)
                .build();
        destinationCardDto = CardDto.builder()
                .id(destinationCardId)
                .number("1111-2222-3333-5555")
                .owner(userDto)
                .expirationDate(LocalDateTime.of(2027,5,19,00,00,00))
                .balance(BigDecimal.valueOf(1500.00))
                .transactionLimitPerDay(BigDecimal.valueOf(1000.00))
                .status(CardStatus.active)
                .build();
        transactionDtoNoId = TransactionDtoNoId.builder()
                .amount(BigDecimal.valueOf(20.00))
                .source(sourceCardDto)
                .destination(destinationCardDto)
                .localDateTime(LocalDateTime.now())
                .build();
        transactionDto = TransactionDto.builder()
                .id(transactionId)
                .amount(BigDecimal.valueOf(20.00))
                .source(sourceCardDto)
                .destination(destinationCardDto)
                .localDateTime(LocalDateTime.now())
                .build();

        transaction = modelMapper.map(transactionDto, Transaction.class);

    }

    @Nested
    class getTransactionById{

        @Test
        public void shouldCallRepository(){
            transactionService.getTransactionById(transactionId);
            verify(transactionRepository).findById(transactionId);

        }

    }

    @Nested
    class addTransaction{

        @Test
        public void shouldCallRepository(){
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            transactionService.addTransaction(transactionDtoNoId);
            verify(transactionRepository).save(any(Transaction.class));
        }

    }

    @Nested
    class updateTransaction{

        @Test
        public void shouldCallRepository(){
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            transactionService.updateTransaction(transactionDto);
            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Nested
    class deleteTransactionById {

        @Test
        public void shouldCallRepository(){
            transactionService.deleteTransactionById(transactionId);
            verify(transactionRepository).deleteById(transactionId);
        }
    }

    @Nested
    class makeTransaction {

        @Test
        public void success(){
            BigDecimal spentToday = new BigDecimal("200.00");
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            Card destinationCard = modelMapper.map(destinationCardDto, Card.class);

            when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
            when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));
            when(transactionRepository.getExpensesForSpecificSourceCardAndForSpecificDay(
                    eq(sourceCardId), any(), any())).thenReturn(spentToday);

            transactionService.makeTransaction(sourceCardId, destinationCardId, transactionDto.getAmount());
            assertEquals(new BigDecimal("980.00"), sourceCard.getBalance());
            assertEquals(new BigDecimal("1520.00"), destinationCard.getBalance());

            verify(cardRepository).save(sourceCard);
            verify(cardRepository).save(destinationCard);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        public void blockedCard_shouldThrowException(){
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            sourceCard.setStatus(CardStatus.blocked);
            Card destinationCard = modelMapper.map(destinationCardDto, Card.class);

            when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
            when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));

            assertThrows(BadRequestException.class, () -> transactionService.makeTransaction(sourceCardId, destinationCardId, transactionDto.getAmount()));
        }

        @Test
        public void expiredCard_shouldThrowException(){
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            Card destinationCard = modelMapper.map(destinationCardDto, Card.class);
            destinationCard.setStatus(CardStatus.expired);

            when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
            when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));

            assertThrows(BadRequestException.class, () -> transactionService.makeTransaction(sourceCardId, destinationCardId, transactionDto.getAmount()));
        }

        @Test
        public void sourceAndDestinationCardsAreTheSame_shouldThrowException(){
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            Card destinationCard = modelMapper.map(sourceCardDto, Card.class);

            when(cardRepository.findById(sourceCard.getId())).thenReturn(Optional.of(sourceCard));
            when(cardRepository.findById(destinationCard.getId())).thenReturn(Optional.of(destinationCard));

            assertThrows(BadRequestException.class, () -> transactionService.makeTransaction(
                    sourceCard.getId(), destinationCard.getId(), transactionDto.getAmount()));
        }

        @Test
        public void diffCardsOwners_shouldThrowException(){
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            sourceCard.setOwner(User.builder().id(UUID.randomUUID()).email("js@gmail.com").password("456wer123").fullName("Name Surname").role(Role.USER).build());
            Card destinationCard = modelMapper.map(destinationCardDto, Card.class);

            when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
            when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));

            assertThrows(BadRequestException.class, () -> transactionService.makeTransaction(sourceCardId, destinationCardId, transactionDto.getAmount()));
        }

        @Test
        public void insufficientFunds_shouldThrowException(){
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            Card destinationCard = modelMapper.map(destinationCardDto, Card.class);

            when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
            when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));

            assertThrows(BadRequestException.class, () -> transactionService.makeTransaction(sourceCardId, destinationCardId, new BigDecimal("1500.00")));
        }

        @Test
        public void exceededLimit_shouldThrowException(){
            BigDecimal spentToday = new BigDecimal("200.00");
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            Card destinationCard = modelMapper.map(destinationCardDto, Card.class);

            when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
            when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));
            when(transactionRepository.getExpensesForSpecificSourceCardAndForSpecificDay(
                    eq(sourceCardId), any(), any())).thenReturn(spentToday);

            assertThrows(BadRequestException.class, () -> transactionService.makeTransaction(sourceCardId, destinationCardId, new BigDecimal("900.00")));
        }
    }

}