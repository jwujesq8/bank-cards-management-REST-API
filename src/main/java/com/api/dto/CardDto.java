package com.api.dto;

import com.api.config.enums.CardStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private UUID id;

    @NotNull(message = "Card must have number")
    private String number;

    @NotNull(message = "Card must have an owner")
    private UserDto owner;

    @NotNull(message = "Card must have expiration date")
    private LocalDateTime expirationDate;

    @NotNull(message = "Card must have status")
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @NotNull(message = "Card must have a balance")
    private BigDecimal balance;

    private BigDecimal transactionLimitPerDay;

}
