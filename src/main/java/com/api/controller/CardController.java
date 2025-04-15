package com.api.controller;

import com.api.dto.*;
import com.api.service.interfaces.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDto> getCardById(@RequestBody IdDto cardIdDto){
        return ResponseEntity.ok(cardService.getCardById(cardIdDto.getId()));
    }

    @PostMapping("/new")
    public ResponseEntity<CardDto> addCard(@RequestBody CardDtoNoId cardDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.addCard(cardDtoNoId));
    }

    @PutMapping
    public ResponseEntity<CardDto> updateCard(@RequestBody CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(cardDto));
    }

    @PutMapping("/status")
    public void updateCardStatus(@RequestBody CardIdStatusDto cardIdStatusDto){
        cardService.updateCardStatus(cardIdStatusDto.getId(), cardIdStatusDto.getNewStatus().name());
    }

    @PutMapping("/transactionLimitPerDay")
    public void updateCardsTransactionLimitPerDayById(@RequestBody CardIdLimitDto cardIdLimitDto){
        cardService.updateCardsTransactionLimitPerDayById(cardIdLimitDto.getCardId(),
                cardIdLimitDto.getNewTransactionLimitPerDay());
    }

    @DeleteMapping
    public void deleteCardById(@RequestBody IdDto cardIdDto){
        cardService.deleteCardById(cardIdDto.getId());
    }

    @GetMapping("/all")
    public ResponseEntity<Page<CardDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/all/owner")
    public ResponseEntity<Page<CardDto>> findAllByOwnerId(@RequestBody IdDto ownerIdDto,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAllByOwnerId(ownerIdDto.getId(), PageRequest.of(page,size)));
    }

//    @PostMapping("/payment")
//    public void makePayment(@RequestBody PaymentDto paymentDto){
//        cardService.makePayment(paymentDto.getSourceCardId(), paymentDto.getDestinationCardId(), paymentDto.getAmount());
//    }
}
