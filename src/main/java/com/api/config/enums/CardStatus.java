package com.api.config.enums;


/**
 * Class CardStatus
 *
 * Enum representing possible statuses for a bank card.
 */
public enum CardStatus {
    /**
     * Active card. Available for operations.
     */
    active,

    /**
     * Blocked card. Not available for transactions.
     */
    blocked,

    /**
     * Expired card. No longer valid.
     */
    expired
}
