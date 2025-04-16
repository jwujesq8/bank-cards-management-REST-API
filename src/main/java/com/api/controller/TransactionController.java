package com.api.controller;

import com.api.dto.IdDto;
import com.api.dto.PaymentDto;
import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.service.interfaces.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    @Operation(summary = "get transaction by id - only for admin and source or destination card owner")
    @PostMapping
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || " +
            "@permissionChecker.isTransactionSourceOrDestinationOwner(#transactionIdDto, authentication.principal))")
    public ResponseEntity<TransactionDto> getTransactionById(@RequestBody @Valid IdDto transactionIdDto){
        return ResponseEntity.ok(transactionService.getTransactionById(transactionIdDto.getId()));
    }

    @Operation(summary = "add new transaction - only for admin")
    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> addTransaction(@RequestBody @Valid TransactionDtoNoId transactionDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(transactionDtoNoId));
    }

    @Operation(summary = "update transaction that already exists in the database - only for admin")
    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> updateTransaction(@RequestBody @Valid TransactionDto transactionDto){
        return ResponseEntity.ok(transactionService.updateTransaction(transactionDto));
    }

    @Operation(summary = "delete transaction by id - only for admin")
    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteTransactionById(@RequestBody @Valid IdDto transactionIdDto){
        transactionService.deleteTransactionById(transactionIdDto.getId());
    }

    @Operation(summary = "make a transaction - only for a source card owner")
    @PostMapping("/make")
    @PreAuthorize("isAuthenticated() && @permissionChecker.isSourceCardOwnerRequestToMakeTransaction(#paymentDto, authentication.principal)")
    public void makeTransaction(@RequestBody @Valid PaymentDto paymentDto){
        transactionService.makeTransaction(paymentDto.getSourceCardId(), paymentDto.getDestinationCardId(), paymentDto.getAmount());
    }

    @Operation(summary = "get all transactions (paging) - only for admin")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(transactionService.findAll(PageRequest.of(page, size)));
    }

    @Operation(summary = "get all transaction by source or destination card id (paging) - only for admin and card owner")
    @PostMapping("/all/card")
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || " +
            "@permissionChecker.isCardOwner(#cardIdDto, authentication.principal))")
    public ResponseEntity<Page<TransactionDto>> findAllByCardId(@RequestBody @Valid IdDto cardIdDto,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(transactionService.findAllByCardId(cardIdDto.getId(),PageRequest.of(page, size)));
    }

}
