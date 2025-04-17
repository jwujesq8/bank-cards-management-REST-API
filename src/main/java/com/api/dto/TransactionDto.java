package com.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Class TransactionDto
 *
 * TransactionDto is a Data Transfer Object (DTO) representing a transaction between two cards.
 * It contains the details of the source card, destination card, transaction date and time, and the transaction amount.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionDto {

    /**
     * The unique identifier (UUID) of the transaction.
     */
    @NotNull(message = "Transaction must have an id")
    @Schema(description = "Transaction id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    /**
     * The source card involved in the transaction.
     */
    @NotNull(message = "Transaction must have a source card")
    @Schema(description = "Source card")
    private CardDto source;

    /**
     * The destination card involved in the transaction.
     */
    @NotNull(message = "Transaction must have a destination card")
    @Schema(description = "Destination card")
    private CardDto destination;

    /**
     * The local date and time when the transaction was made.
     */
    @NotNull(message = "Transaction must have local date and time")
    @Schema(description = "Transaction local date and time", example = "2029-04-30T00:00:00")
    private LocalDateTime localDateTime;

    /**
     * The amount of the transaction.
     */
    @NotNull(message = "Transaction must have an amount")
    @Schema(description = "Transaction amount", example = "1000.00")
    private BigDecimal amount;
}
