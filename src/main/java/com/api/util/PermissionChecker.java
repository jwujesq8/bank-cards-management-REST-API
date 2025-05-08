package com.api.util;

import com.api.dto.IdDto;
import com.api.dto.PaymentDto;
import com.api.exception.ForbiddenException;
import com.api.repository.CardRepository;
import com.api.repository.TransactionRepository;
import com.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Class PermissionChecker
 *
 * Utility component for checking user permissions based on ownership.
 * Used to validate whether the authenticated user has access to specific cards or transactions.
 */
@RequiredArgsConstructor
@Component
public class PermissionChecker {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Checks if the user is the owner of the specified card.
     * Used in CardController.getCardById() and TransactionController.findAllByCardId()
     *
     * @param idDto contains the card ID
     * @param email user's email
     * @return true if the user is the card owner
     * @throws ForbiddenException if the user is not the owner
     */
    public boolean isCardOwner(IdDto idDto, String email) {
        if(cardRepository.existsByIdAndOwnerEmail(idDto.getId(), email)) return true;
        else throw new ForbiddenException("Only cards owner and admin have access");
    }

    /**
     * Checks if the user is the owner of the source or destination card in a transaction.
     * Used in TransactionController.getTransactionById()
     *
     * @param transactionIdDto contains the transaction ID
     * @param email user's email
     * @return true if the user is the source or destination card owner
     * @throws ForbiddenException if the user is not the owner
     */
    public boolean isTransactionSourceOrDestinationOwner(IdDto transactionIdDto, String email){
        if(transactionRepository.existsByIdAndSourceOwnerEmail(transactionIdDto.getId(), email)) return true;
        if(transactionRepository.existsByIdAndDestinationOwnerEmail(transactionIdDto.getId(), email)) return true;
        else throw new ForbiddenException("Only cards owner has access");
    }

    /**
     * Checks if the user is requesting their own cards by owner ID.
     * Used in CardController.findAllByOwnerId()
     *
     * @param ownerIdDto contains the user ID
     * @param email user's email
     * @return true if the request is made by the owner
     * @throws ForbiddenException if the user is not the owner
     */
    public boolean isOwnerRequestToFindAllHisCards(IdDto ownerIdDto, String email){
        if(userRepository.existsByIdAndEmail(ownerIdDto.getId(), email)) return true;
        else throw new ForbiddenException("Only cards owner and admin have access");
    }

    /**
     * Checks if the user is the owner of the source card in a transaction.
     * Used in TransactionController.makeTransaction()
     *
     * @param paymentDto contains the source card ID
     * @param email user's email
     * @return true if the user owns the source card
     * @throws ForbiddenException if the user is not the owner
     */
    public boolean isSourceCardOwnerRequestToMakeTransaction(PaymentDto paymentDto, String email){
        if(cardRepository.existsByIdAndOwnerEmail(paymentDto.getSourceCardId(), email)) return true;
        else throw new ForbiddenException("Only cards owner has access");
    }

}