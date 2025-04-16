package com.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PaymentDto {

    @NotNull(message = "Card id can't be null")
    private UUID sourceCardId;

    @NotNull(message = "Card id can't be null")
    private UUID destinationCardId;

    @NotNull(message = "Transaction amount can't be null")
    @DecimalMin(value = "1.00", message = "Min transaction amount is 1.00")
    private BigDecimal amount;

    public PaymentDto(UUID sourceCardId, UUID destinationCardId, BigDecimal amount) {
        this.sourceCardId = sourceCardId;
        this.destinationCardId = destinationCardId;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
}
