package com.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Card id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotNull(message = "Card transaction limit per day can't be null")
    @DecimalMin(value = "100.00", message = "Min card limit is 100")
    @Schema(description = "Card new transaction limit per day", example = "1000.00")
    private BigDecimal newTransactionLimitPerDay;

    public CardIdLimitDto(UUID id, BigDecimal newTransactionLimitPerDay) {
        this.id = id;
        this.newTransactionLimitPerDay = newTransactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);
    }

    public void setNewTransactionLimitPerDay(BigDecimal newTransactionLimitPerDay) {
        this.newTransactionLimitPerDay = newTransactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);
    }

}
