package com.api.config;

import com.api.dto.IdDto;
import com.api.exception.BadRequestException;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PermissionChecker {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    // where used - TransactionController.findAllByCardId()
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


    // TODO: doesOwnerRequestFindAllHisCards
    // TODO: doesOwnerRequestGetHisOwnCard
    // TODO: doesOwnerRequestFindAllHisTransactionsByCard

}