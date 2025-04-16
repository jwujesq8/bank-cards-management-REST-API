package com.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionDto {

    @NotNull(message = "Transaction must have an id")
    @Schema(description = "Transaction id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotNull(message = "Transaction must have a source card")
    @Schema(description = "Source card")
    private CardDto source;

    @NotNull(message = "Transaction must have a destination card")
    @Schema(description = "Destination card")
    private CardDto destination;

    @NotNull(message = "Transaction must have local date and time")
    @Schema(description = "Transaction local date and time", example = "2029-04-30T00:00:00")
    private LocalDateTime localDateTime;

    @NotNull(message = "Transaction must have an amount")
    @Schema(description = "Transaction amount", example = "1000.00")
    private BigDecimal amount;
}
