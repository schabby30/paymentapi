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
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.kibit.paymentapi.util.PaymentUtil.createResponse;

@Service
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String paymentTopic;


    public PaymentService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          KafkaTemplate<String, String> kafkaTemplate,
                          @Value("${payment.kafka.topic}") String paymentTopic) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentTopic = paymentTopic;
    }

    @Transactional
    public ResponseEntity<TransactionResponseDto> processPayment(PaymentRequestDto request) {

        // Check if transaction already exists
        if (transactionRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new IdempotencyKeyExistsException("Transactions must be unique");
        }

        // Check if amount is positive
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.valueOf(0)) < 1) {
            throw new PaymentProcessingException("Amount must be greater than zero");
        }

        // Fetch sender and recipient accounts
        Account sender = accountRepository.findByAccountNumberAndLockRow(request.getSenderAccount())
                .orElseThrow(() -> new PaymentProcessingException("Sender account not found"));

        Account recipient = accountRepository.findByAccountNumber(request.getRecipientAccount())
                .orElseThrow(() -> new PaymentProcessingException("Recipient account not found"));

        // Check if sender has enough balance
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance to process the transaction");
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

        // Send payment notification via Kafka
        sendPaymentNotification(transaction);

        return createResponse(true, HttpStatus.OK, transaction.toString());
    }

    private void sendPaymentNotification(Transaction transaction) {
        String message = "Payment of " + transaction.getAmount() +
                " from " + transaction.getSenderAccount() +
                " to " + transaction.getRecipientAccount() + " was successful.";

        kafkaTemplate.send(paymentTopic, message);
    }
}
