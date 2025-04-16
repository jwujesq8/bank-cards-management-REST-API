package com.api.dto;

import com.api.config.enums.CardStatus;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CardDtoNoId {

    @NotNull(message = "Card must have number")
    @NotEmpty(message = "Card number can't be empty")
    @Pattern(regexp = "\\d{4}-\\d{4}-\\d{4}-\\d{4}", message = "Non valid number")
    private String number;

    @NotNull(message = "Card must have an owner")
    private UserDto owner;

    @NotNull(message = "Card must have expiration date")
    private LocalDateTime expirationDate;

    @NotNull(message = "Card must have status")
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @NotNull(message = "Card must have a balance")
    @DecimalMin(value = "0", message = "Min balance is 0")
    private BigDecimal balance;

    @DecimalMin(value = "100", message = "Min limit is 100")
    private BigDecimal transactionLimitPerDay;
}
