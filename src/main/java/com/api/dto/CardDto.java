package com.api.dto;

import com.api.config.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CardDto {

    @NotNull(message = "Card must have id")
    @Schema(description = "Card id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotNull(message = "Card must have number")
    @NotEmpty(message = "Card number can't be empty")
    @Pattern(regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}", message = "Non valid number")
    @Schema(description = "Card number", example = "normal: 0000-0000-0000-0000, masked: ****-****-****-0000")
    private String number;

    @NotNull(message = "Card must have an owner")
    @Schema(description = "Card owner")
    private UserDto owner;

    @NotNull(message = "Card must have expiration date")
    @Schema(description = "Card id", example = "2029-04-30T00:00:00")
    private LocalDateTime expirationDate;

    @NotNull(message = "Card must have status")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Card status (active|blocked|expired)", example = "expired")
    private CardStatus status;

    @NotNull(message = "Card must have a balance")
    @DecimalMin(value = "0", message = "Min balance is 0")
    @Schema(description = "Card balance", example = "1000.00")
    private BigDecimal balance;

    @DecimalMin(value = "100", message = "Min limit is 100")
    @Schema(description = "Card transaction limit per day", example = "1000.00")
    private BigDecimal transactionLimitPerDay;

}
