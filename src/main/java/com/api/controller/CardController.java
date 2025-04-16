package com.api.controller;

import com.api.dto.*;
import com.api.service.interfaces.CardService;
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
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @Operation(summary = "get card by id - only for admin and card owner")
    @PostMapping
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || @permissionChecker.isCardOwner(#cardIdDto, authentication.principal))")
    public ResponseEntity<CardDto> getCardById(@RequestBody @Valid IdDto cardIdDto){
        return ResponseEntity.ok(cardService.getCardById(cardIdDto.getId()));
    }

    @Operation(summary = "add a new card - only for admin")
    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<CardDto> addCard(@RequestBody @Valid CardDtoNoId cardDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.addCard(cardDtoNoId));
    }

    @Operation(summary = "update card that already exists in the database - only for admin")
    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateCard(@RequestBody @Valid CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(cardDto));
    }

    @Operation(summary = "update only cards status - only for admin")
    @PutMapping("/status")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void updateCardStatus(@RequestBody @Valid CardIdStatusDto cardIdStatusDto){
        cardService.updateCardStatus(cardIdStatusDto.getId(), cardIdStatusDto.getNewStatus().name());
    }

    @Operation(summary = "update only cards transaction limit per date - only for admin")
    @PutMapping("/transactionLimitPerDay")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void updateCardsTransactionLimitPerDayById(@RequestBody @Valid CardIdLimitDto cardIdLimitDto){
        cardService.updateCardsTransactionLimitPerDayById(cardIdLimitDto.getId(),
                cardIdLimitDto.getNewTransactionLimitPerDay());
    }

    @Operation(summary = "delete card by id - only for admin")
    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteCardById(@RequestBody @Valid IdDto cardIdDto){
        cardService.deleteCardById(cardIdDto.getId());
    }

    @Operation(summary = "get all cards (paging) - only for admin")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAll(PageRequest.of(page, size)));
    }

    @Operation(summary = "get all cards by card owner id (paging) - only for admin and cards owner")
    @GetMapping("/all/owner")
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || @permissionChecker.isOwnerRequestToFindAllHisCards(#ownerIdDto, authentication.principal))")
    public ResponseEntity<Page<CardDto>> findAllByOwnerId(@RequestBody @Valid IdDto ownerIdDto,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAllByOwnerId(ownerIdDto.getId(), PageRequest.of(page,size)));
    }

}
