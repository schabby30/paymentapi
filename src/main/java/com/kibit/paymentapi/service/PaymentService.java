package com.kibit.paymentapi.service;

import com.kibit.paymentapi.dto.PaymentRequestDto;
import com.kibit.paymentapi.dto.TransactionResponseDto;
import com.kibit.paymentapi.exception.IdempotencyKeyExistsException;
import com.kibit.paymentapi.exception.InsufficientBalanceException;
import com.kibit.paymentapi.exception.PaymentProcessingException;
import com.kibit.paymentapi.model.Account;
import com.kibit.paymentapi.model.Transaction;
import com.kibit.paymentapi.repository.AccountRepository;
import com.kibit.paymentapi.repository.TransactionRepository;
import com.kibit.paymentapi.util.PaymentUtil;
import jakarta.transaction.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import java.time.Instant;

import static com.kibit.paymentapi.util.PaymentUtil.createResponse;


@Service
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;


    public PaymentService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            backoff = @Backoff(delay = 100),
            recover = "fallback"
    )
    @Transactional
    public ResponseEntity<TransactionResponseDto> processPayment(PaymentRequestDto request) {

        if (transactionRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new IdempotencyKeyExistsException("Idempotency key already exists");
        }

        // Fetch sender and recipient accounts
        Account sender = accountRepository.findByAccountNumberAndLockRow(request.getSenderAccount())
                .orElseThrow(() -> new PaymentProcessingException("Sender account not found"));

        Account recipient = accountRepository.findByAccountNumber(request.getRecipientAccount())
                .orElseThrow(() -> new PaymentProcessingException("Recipient account not found"));

        // Check if sender has enough balance
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance to process the transaction.");
        }

        // Deduct amount from sender
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        // Add amount to recipient
        recipient.setBalance(recipient.getBalance().add(request.getAmount()));

        // Save accounts with updated balances
        accountRepository.save(sender);
        accountRepository.save(recipient);

        // Save transaction record
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.getIdempotencyKey());
        transaction.setSenderAccount(request.getSenderAccount());
        transaction.setRecipientAccount(request.getRecipientAccount());
        transaction.setAmount(request.getAmount());
        transaction.setTimestamp(Instant.now());

        transactionRepository.save(transaction);

        return createResponse(true, HttpStatus.OK, transaction.toString());
    }

    private ResponseEntity<TransactionResponseDto> fallback() {
        return createResponse(false,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something unexpected happened, the transaction was not successful.");
    }
}
