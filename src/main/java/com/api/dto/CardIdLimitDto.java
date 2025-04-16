package com.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class CardIdLimitDto {

    @NotNull(message = "Card id can't be null")
    private UUID id;

    @NotNull(message = "Card transaction limit per day can't be null")
    @DecimalMin(value = "100.00", message = "Min card limit is 100")
    private BigDecimal newTransactionLimitPerDay;

    public CardIdLimitDto(UUID id, BigDecimal newTransactionLimitPerDay) {
        this.id = id;
        this.newTransactionLimitPerDay = newTransactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);
    }

    public void setNewTransactionLimitPerDay(BigDecimal newTransactionLimitPerDay) {
        this.newTransactionLimitPerDay = newTransactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);
    }

}
