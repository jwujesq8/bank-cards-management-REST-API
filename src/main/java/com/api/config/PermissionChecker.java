package com.api.config;

import com.api.dto.IdDto;
import com.api.dto.PaymentDto;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import com.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PermissionChecker {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // where used - TransactionController.findAllByCardId()
    //              CardController.getCardById()
    public boolean isCardOwner(IdDto idDto, String email) {
        if(cardRepository.existsByIdAndOwnerEmail(idDto.getId(), email)) return true;
        else throw new BadRequestException("Only cards owner and admin have access");
    }

    // where used - TransactionController.getTransactionById()
    public boolean isTransactionSourceOrDestinationOwner(IdDto transactionIdDto, String email){
        if(transactionRepository.existsByIdAndSourceOwnerEmail(transactionIdDto.getId(), email)) return true;
        if(transactionRepository.existsByIdAndDestinationOwnerEmail(transactionIdDto.getId(), email)) return true;
        else throw new BadRequestException("Only cards owner has access");
    }

    // where used - CardController.findAllByOwnerId()
    public boolean isOwnerRequestToFindAllHisCards(IdDto ownerIdDto, String email){
        if(userRepository.existsByIdAndEmail(ownerIdDto.getId(), email)) return true;
        else throw new BadRequestException("Only cards owner and admin have access");
    }

    // where used - TransactionController.makeTransaction()
    public boolean isSourceCardOwnerRequestToMakeTransaction(PaymentDto paymentDto, String email){
        if(cardRepository.existsByIdAndOwnerEmail(paymentDto.getSourceCardId(), email)) return true;
        else throw new BadRequestException("Only cards owner has access");
    }

}