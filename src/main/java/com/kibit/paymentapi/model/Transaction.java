package com.kibit.paymentapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(nullable = false)
    private String senderAccount;

    @Column(nullable = false)
    private String recipientAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant timestamp;
}