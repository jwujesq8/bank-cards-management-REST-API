package com.api.dto;

import com.api.config.enums.CardStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardDtoNoId {

    @NotNull(message = "Card must have number")
    private String number; // TODO: encrypted, Masked

    @NotNull(message = "Card must have an owner")
    private UserDto owner;

    @NotNull(message = "Card must have expiration date")
    private Date expirationDate;

    @NotNull(message = "Card must have status")
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @NotNull(message = "Card must have a balance")
    private BigDecimal balance;

    private BigDecimal transactionLimitPerDay;
}
