package com.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Class PaymentDto
 *
 * PaymentDto is a Data Transfer Object (DTO) used for transferring payment transaction details.
 * It contains information about the source card, destination card, and the transaction amount.
 * This DTO is typically used in payment processing operations.
 */
@NoArgsConstructor
@Getter
@Setter
public class PaymentDto {

    /**
     * The unique identifier (UUID) of the source card for the payment.
     * @return the source card ID.
     */
    @NotNull(message = "Card id can't be null")
    @Schema(description = "Source card id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID sourceCardId;

    /**
     * The unique identifier (UUID) of the destination card for the payment.
     * @return the destination card ID.
     */
    @NotNull(message = "Card id can't be null")
    @Schema(description = "Destination card id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID destinationCardId;

    /**
     * The amount of the transaction (payment) to be processed.
     * The value must be greater than or equal to 1.00.
     * @return the payment amount.
     */
    @NotNull(message = "Transaction amount can't be null")
    @DecimalMin(value = "1.00", message = "Min transaction amount is 1.00")
    @Schema(description = "Payment amount", example = "1000.00")
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
