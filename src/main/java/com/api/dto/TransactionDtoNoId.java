package com.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDtoNoId {

    @NotNull(message = "Transaction must have local date and time")
    private LocalDateTime localDateTime;

    @NotNull(message = "Transaction must have an amount")
    private BigDecimal amount;
}
