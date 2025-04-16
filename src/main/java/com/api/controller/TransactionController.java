package com.api.controller;

import com.api.dto.*;
import com.api.dto.error.ValidationErrorMessageResponseDto;
import com.api.service.interfaces.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Validated
@Tag(name="Transaction controller", description="Manipulations with users cards transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "get transaction by id - only for admin and source or destination card owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = TransactionDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || " +
            "@permissionChecker.isTransactionSourceOrDestinationOwner(#transactionIdDto, authentication.principal))")
    public ResponseEntity<TransactionDto> getTransactionById(@RequestBody @Valid IdDto transactionIdDto){
        return ResponseEntity.ok(transactionService.getTransactionById(transactionIdDto.getId()));
    }

    @Operation(summary = "add new transaction - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "New transaction is created", content = @Content(schema = @Schema(implementation = TransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> addTransaction(@RequestBody @Valid TransactionDtoNoId transactionDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(transactionDtoNoId));
    }

    @Operation(summary = "update transaction that already exists in the database - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction is updated", content = @Content(schema = @Schema(implementation = TransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<TransactionDto> updateTransaction(@RequestBody @Valid TransactionDto transactionDto){
        return ResponseEntity.ok(transactionService.updateTransaction(transactionDto));
    }

    @Operation(summary = "delete transaction by id - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction is deleted or does not exist"),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteTransactionById(@RequestBody @Valid IdDto transactionIdDto){
        transactionService.deleteTransactionById(transactionIdDto.getId());
    }

    @Operation(summary = "make a transaction - only for a source card owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction is made"),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PostMapping("/make")
    @PreAuthorize("isAuthenticated() && @permissionChecker.isSourceCardOwnerRequestToMakeTransaction(#paymentDto, authentication.principal)")
    public void makeTransaction(@RequestBody @Valid PaymentDto paymentDto){
        transactionService.makeTransaction(paymentDto.getSourceCardId(), paymentDto.getDestinationCardId(), paymentDto.getAmount());
    }

    @Operation(summary = "get all transactions (paging) - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(transactionService.findAll(PageRequest.of(page, size)));
    }

    @Operation(summary = "get all transaction by source or destination card id (paging) - only for admin and card owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = TransactionDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
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
