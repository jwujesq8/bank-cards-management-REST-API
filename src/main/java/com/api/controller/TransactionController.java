package com.api.controller;

import com.api.dto.IdDto;
import com.api.dto.PaymentDto;
import com.api.dto.TransactionDto;
import com.api.dto.TransactionDtoNoId;
import com.api.service.interfaces.TransactionService;
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

    @PostMapping
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || " +
            "@permissionChecker.isTransactionSourceOrDestinationOwner(#transactionIdDto, authentication.principal))")
    public ResponseEntity<TransactionDto> getTransactionById(@RequestBody @Valid IdDto transactionIdDto){
        return ResponseEntity.ok(transactionService.getTransactionById(transactionIdDto.getId()));
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> addTransaction(@RequestBody @Valid TransactionDtoNoId transactionDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(transactionDtoNoId));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> updateTransaction(@RequestBody @Valid TransactionDto transactionDto){
        return ResponseEntity.ok(transactionService.updateTransaction(transactionDto));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteTransactionById(@RequestBody @Valid IdDto transactionIdDto){
        transactionService.deleteTransactionById(transactionIdDto.getId());
    }

    @PostMapping("/make")
    @PreAuthorize("isAuthenticated() && @permissionChecker.isSourceCardOwnerRequestToMakeTransaction(#paymentDto, authentication.principal)")
    public void makeTransaction(@RequestBody @Valid PaymentDto paymentDto){
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
    public ResponseEntity<Page<TransactionDto>> findAllByCardId(@RequestBody @Valid IdDto cardIdDto,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(transactionService.findAllByCardId(cardIdDto.getId(),PageRequest.of(page, size)));
    }

}
