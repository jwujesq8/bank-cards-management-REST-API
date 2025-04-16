package com.api.controller;

import com.api.dto.*;
import com.api.dto.error.ValidationErrorMessageResponseDto;
import com.api.service.interfaces.CardService;
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
@RequestMapping("/card")
@RequiredArgsConstructor
@Validated
@Tag(name="Card controller", description="Manipulations with users cards")
public class CardController {

    private final CardService cardService;

    @Operation(summary = "get card by id - only for admin and card owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = CardDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || @permissionChecker.isCardOwner(#cardIdDto, authentication.principal))")
    public ResponseEntity<CardDto> getCardById(@RequestBody @Valid IdDto cardIdDto){
        return ResponseEntity.ok(cardService.getCardById(cardIdDto.getId()));
    }

    @Operation(summary = "add a new card - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "New card is created", content = @Content(schema = @Schema(implementation = CardDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<CardDto> addCard(@RequestBody @Valid CardDtoNoId cardDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.addCard(cardDtoNoId));
    }

    @Operation(summary = "update card that already exists in the database - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card is updated", content = @Content(schema = @Schema(implementation = CardDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateCard(@RequestBody @Valid CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(cardDto));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card status is updated"),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @Operation(summary = "update only cards status - only for admin")
    @PutMapping("/status")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void updateCardStatus(@RequestBody @Valid CardIdStatusDto cardIdStatusDto){
        cardService.updateCardStatus(cardIdStatusDto.getId(), cardIdStatusDto.getNewStatus().name());
    }

    @Operation(summary = "update only cards transaction limit per date - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cards transaction limit per date is updated"),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PutMapping("/transactionLimitPerDay")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void updateCardsTransactionLimitPerDayById(@RequestBody @Valid CardIdLimitDto cardIdLimitDto){
        cardService.updateCardsTransactionLimitPerDayById(cardIdLimitDto.getId(),
                cardIdLimitDto.getNewTransactionLimitPerDay());
    }

    @Operation(summary = "delete card by id - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card is deleted or does not exist"),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteCardById(@RequestBody @Valid IdDto cardIdDto){
        cardService.deleteCardById(cardIdDto.getId());
    }

    @Operation(summary = "get all cards (paging) - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAll(PageRequest.of(page, size)));
    }

    @Operation(summary = "get all cards by card owner id (paging) - only for admin and cards owner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @GetMapping("/all/owner")
    @PreAuthorize("isAuthenticated() && " +
            "(hasRole('ADMIN') || @permissionChecker.isOwnerRequestToFindAllHisCards(#ownerIdDto, authentication.principal))")
    public ResponseEntity<Page<CardDto>> findAllByOwnerId(@RequestBody @Valid IdDto ownerIdDto,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(cardService.findAllByOwnerId(ownerIdDto.getId(), PageRequest.of(page,size)));
    }

}
