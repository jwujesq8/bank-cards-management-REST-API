package com.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardDto {

    @NotNull(message = "Card must have id")
    private UUID id;

    @NotNull(message = "Card must have number")
    private String number; // TODO: encrypted, Masked

    @NotNull(message = "Card must have an owner")
    private UserDto owner;

    @NotNull(message = "Card must have expiration date")
    private Date expirationDate;

    @NotNull(message = "Card must have status")
    @Pattern(regexp = "^(активна|заблокирована|истек срок действия)$",
            message = "Statuses that are acceptable: активна|заблокирована|истек срок действия")
    private String status;

    @NotNull(message = "Card must have a balance")
    private BigDecimal balance;

    private BigDecimal transactionLimitPerDay;

}
