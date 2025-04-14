package com.api.entity;

import com.api.dto.TransactionDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cards")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Card {

    /**
     * The unique identifier of the card.
     * This is a UUID that uniquely identifies the card.
     */
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    private String number; // TODO: encrypted, Masked

    /**
     * The owner of the card.
     */
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "expiration_date")
    private Date expirationDate;

    private String status;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "transaction_limit_per_day", precision = 10, scale = 2)
    private BigDecimal transactionLimitPerDay;

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TransactionDto> transactionsHistory;

    public Card(String number, User owner, Date expirationDate,String status,
                BigDecimal balance, BigDecimal transactionLimitPerDay,
                List<TransactionDto> transactionsHistory) {
        this.number = number;
        this.owner = owner;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);;
        this.transactionLimitPerDay = transactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);;
        this.transactionsHistory = transactionsHistory;
    }


    public void setBalance(BigDecimal balance) {
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public void setTransactionLimitPerDay(BigDecimal transactionLimitPerDay) {
        this.transactionLimitPerDay = transactionLimitPerDay.setScale(2, RoundingMode.HALF_UP);
    }

}
