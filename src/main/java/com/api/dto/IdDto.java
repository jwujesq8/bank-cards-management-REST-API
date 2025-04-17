package com.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

/**
 * Class IdDto
 *
 * IdDto is a Data Transfer Object (DTO) used to transfer a single identifier (UUID).
 * It is typically used for operations where a unique ID is required for querying, updating, or deleting an entity.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IdDto {

    /**
     * The unique identifier (UUID) of the entity.
     */
    @NotNull(message = "Id can't be null")
    @Schema(description = "Id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

}
