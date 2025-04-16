package com.api.controller;

import com.api.dto.IdDto;
import com.api.dto.PaymentDto;
import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || " +
            "@permissionChecker.isTransactionSourceOrDestinationOwner(#transactionIdDto, authentication.principal))")
    public ResponseEntity<TransactionDto> getTransactionById(@RequestBody IdDto transactionIdDto){
        return ResponseEntity.ok(transactionService.getTransactionById(transactionIdDto.getId()));
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> addTransaction(@RequestBody TransactionDtoNoId transactionDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(transactionDtoNoId));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> updateTransaction(@RequestBody TransactionDto transactionDto){
        return ResponseEntity.ok(transactionService.updateTransaction(transactionDto));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteTransactionById(@RequestBody IdDto transactionIdDto){
        transactionService.deleteTransactionById(transactionIdDto.getId());
    }

    @PostMapping("/make") // TODO: only for owners
    @PreAuthorize("isAuthenticated() && @permissionChecker.isSourceCardOwnerRequestToMakeTransaction(#paymentDto, authentication.principal)")
    public void makeTransaction(@RequestBody PaymentDto paymentDto){
        transactionService.makeTransaction(paymentDto.getSourceCardId(), paymentDto.getDestinationCardId(), paymentDto.getAmount());
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(transactionService.findAll(PageRequest.of(page, size)));
    }

    @PostMapping("/all/card")
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || " +
            "@permissionChecker.isCardOwner(#cardIdDto, authentication.principal))")
    public ResponseEntity<Page<TransactionDto>> findAllByCardId(@RequestBody IdDto cardIdDto,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(transactionService.findAllByCardId(cardIdDto.getId(),PageRequest.of(page, size)));
    }

}
