package com.api.controller;

import com.api.dto.*;
import com.api.service.interfaces.CardService;
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

    @PostMapping
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || @permissionChecker.isCardOwner(#cardIdDto, authentication.principal))")
    public ResponseEntity<CardDto> getCardById(@RequestBody @Valid IdDto cardIdDto){
        return ResponseEntity.ok(cardService.getCardById(cardIdDto.getId()));
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<CardDto> addCard(@RequestBody @Valid CardDtoNoId cardDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.addCard(cardDtoNoId));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateCard(@RequestBody @Valid CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(cardDto));
    }

    @PutMapping("/status")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void updateCardStatus(@RequestBody @Valid CardIdStatusDto cardIdStatusDto){
        cardService.updateCardStatus(cardIdStatusDto.getId(), cardIdStatusDto.getNewStatus().name());
    }

    @PutMapping("/transactionLimitPerDay")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void updateCardsTransactionLimitPerDayById(@RequestBody @Valid CardIdLimitDto cardIdLimitDto){
        cardService.updateCardsTransactionLimitPerDayById(cardIdLimitDto.getId(),
                cardIdLimitDto.getNewTransactionLimitPerDay());
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteCardById(@RequestBody @Valid IdDto cardIdDto){
        cardService.deleteCardById(cardIdDto.getId());
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/all/owner")
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || @permissionChecker.isOwnerRequestToFindAllHisCards(#ownerIdDto, authentication.principal))")
    public ResponseEntity<Page<CardDto>> findAllByOwnerId(@RequestBody @Valid IdDto ownerIdDto,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAllByOwnerId(ownerIdDto.getId(), PageRequest.of(page,size)));
    }

}
