package com.api.service;

import com.api.config.enums.CardStatus;
import com.api.config.enums.Role;
import com.api.dto.CardDto;
import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.dto.UserDto;
import com.api.entity.Card;
import com.api.entity.Transaction;
import com.api.exception.BadRequestException;
import com.api.repository.TransactionRepository;
import com.api.service.executor.interfaces.InternalTransactionExecutor;
import com.api.service.validation.TransactionValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
class TransactionServiceImplTest {

    private TransactionServiceImpl transactionService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private InternalTransactionExecutor internalTransactionExecutor;
    @Mock
    private TransactionValidator transactionValidator;
    private ModelMapper modelMapper;

    private UUID transactionId;
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
        modelMapper = new ModelMapper(); // basic ModelMapper for less coding but without ours ModelMapper config
        transactionService = new TransactionServiceImpl(
                transactionRepository,
                modelMapper,
                internalTransactionExecutor,
                transactionValidator);
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
            transactionService.addTransaction(modelMapper.map(transactionDto, TransactionDtoNoId.class));
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
            Card sourceCard = modelMapper.map(sourceCardDto, Card.class);
            Card destinationCard = modelMapper.map(destinationCardDto, Card.class);
            TransactionValidator.SourceAndDestinationCards sourceAndDestinationCards =
                    new TransactionValidator.SourceAndDestinationCards(sourceCard, destinationCard);
            when(transactionValidator.makeTransaction_validateCardsAndAmount(
                        eq(sourceCardId),
                        eq(destinationCardId),
                    any(BigDecimal.class)))
                .thenReturn(sourceAndDestinationCards);
            doNothing().when(internalTransactionExecutor).performTransaction(
                    sourceCard,
                    destinationCard,
                    transactionDto.getAmount());

            transactionService.makeTransaction(sourceCardId, destinationCardId, transactionDto.getAmount());

            verify(internalTransactionExecutor).performTransaction(sourceCard,destinationCard,transactionDto.getAmount());
        }

        @Test
        public void failure_MockValidatorThrowsException(){
            when(transactionValidator.makeTransaction_validateCardsAndAmount(
                    eq(sourceCardId),
                    eq(destinationCardId),
                    any(BigDecimal.class)))
                    .thenThrow(new BadRequestException("Bad request exception message"));

            assertThrows(BadRequestException.class,
                    () -> transactionService.makeTransaction(sourceCardId, destinationCardId, transactionDto.getAmount()));
        }
    }
}