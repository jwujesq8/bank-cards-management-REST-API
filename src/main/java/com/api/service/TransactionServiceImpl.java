package com.api.service;

import com.api.config.enums.CardStatus;
import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.entity.Card;
import com.api.entity.Transaction;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import com.api.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;

    /**
     * @param transactionId
     * @return
     */
    @Override
    public TransactionDto getTransactionById(UUID transactionId) {
        return modelMapper.map(transactionRepository.findById(transactionId), TransactionDto.class);
    }

    /**
     * @param transactionDtoNoId
     * @return
     */
    @Override
    public TransactionDto addTransaction(TransactionDtoNoId transactionDtoNoId) {
        Transaction transaction = transactionRepository.save(modelMapper.map(transactionDtoNoId, Transaction.class));
        return modelMapper.map(transaction, TransactionDto.class);
    }

    /**
     * @param transactionDto
     * @return
     */
    @Override
    public TransactionDto updateTransaction(TransactionDto transactionDto) {
        Transaction transaction = transactionRepository.save(modelMapper.map(transactionDto, Transaction.class));
        return modelMapper.map(transaction, TransactionDto.class);
    }

    /**
     * @param transactionId
     */
    @Override
    public void deleteTransactionById(UUID transactionId) {
        transactionRepository.deleteById(transactionId);
    }


    /**
     * @param pageable
     * @return
     */
    @Override
    public Page<TransactionDto> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(
                transaction -> modelMapper.map(transaction, TransactionDto.class));
    }

    /**
     * @param sourceCardId
     * @param destinationCardId
     * @param amount
     */
    @Transactional
    @Override
    public void makeTransaction(UUID sourceCardId, UUID destinationCardId, BigDecimal amount){
        // Check if both cards exist
        Card sourceCard = cardRepository.findById(sourceCardId).orElseThrow(
                () -> new BadRequestException("There is no such source card")
        );
        Card destinationCard = cardRepository.findById(destinationCardId).orElseThrow(
                () -> new BadRequestException("There is no such destination card")
        );
        // Throw exception if at least one card is not active
        if (!CardStatus.active.equals(sourceCard.getStatus())) {
            throw new BadRequestException("Source card is not active or expired");
        }
        if (!CardStatus.active.equals(destinationCard.getStatus())) {
            throw new BadRequestException("Destination card is not active or expired");
        }
        // Check if source and destination card are different
        if (sourceCard.equals(destinationCard)) {
            throw new BadRequestException("Source card and destination card must be different");
        }
        // Check if both source and destination cards have the same owner
        if (!sourceCard.getOwner().equals(destinationCard.getOwner())){
            throw new BadRequestException("Transactions may only occur between cards of the same owner");
        }
        // Check if source card has sufficient funds to make the transfer
        if (sourceCard.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }
        // Check if source card does not exceed the limit
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.atStartOfDay();
        LocalDateTime end = now.plusDays(1).atStartOfDay();
        BigDecimal expensesForToday = transactionRepository.getExpensesForSpecificSourceCardAndForSpecificDay(
                sourceCardId, start, end);
        if(sourceCard.getTransactionLimitPerDay().compareTo(expensesForToday.add(amount))<0){
            throw new BadRequestException("Source card has a limit per one day: " + sourceCard.getTransactionLimitPerDay() +
                    ". Your expenses for today are: " + expensesForToday);
        }


        // Plus amount to the destination and minus from the source
        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destinationCard.setBalance(destinationCard.getBalance().add(amount));

        // Update cards with new balances
        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);

        // Add transaction to the database
        Transaction transaction = new Transaction(
                sourceCard, destinationCard, LocalDateTime.now(), amount
        );
        transactionRepository.save(transaction);
    }

    @Override
    public Page<TransactionDto> findAllByCardId(UUID cardId, Pageable pageable){
        return transactionRepository.findAllByCardId(cardId, pageable)
                .map(transaction -> modelMapper.map(transaction, TransactionDto.class));
    }
}
