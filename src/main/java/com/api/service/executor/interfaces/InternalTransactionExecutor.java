package com.api.service.executor.interfaces;

import com.api.entity.Card;

import java.math.BigDecimal;
import java.util.UUID;

public interface InternalTransactionExecutor {

    void performTransaction(Card sourceCard, Card destinationCard, BigDecimal amount);
}
