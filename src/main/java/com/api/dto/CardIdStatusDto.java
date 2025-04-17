package com.api.dto;

import com.api.config.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

/**
 * Class CardIdStatusDto
 *
 * CardIdStatusDto is a Data Transfer Object (DTO) used for updating the status of a specific card.
 * It includes the card's ID and the new status to be applied.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardIdStatusDto {

    /**
     * The ID of the card.
     * @return the card's ID.
     */
    @NotNull(message = "Card id can't be null")
    @Schema(description = "Card id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    /**
     * The new status to be applied to the card.
     * The status can be one of the following: active, blocked, or expired.
     * @return the new status of the card.
     */
    @NotNull(message = "New cards status can't be null")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Card status (active|blocked|expired)", example = "expired")
    private CardStatus newStatus;

}
