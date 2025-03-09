package com.kibit.paymentapi.repository;

import com.kibit.paymentapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Boolean existsByIdempotencyKey(UUID idempotencyKey);

}
