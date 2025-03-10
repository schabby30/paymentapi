package com.kibit.paymentapi;

import com.kibit.paymentapi.dto.PaymentRequestDto;
import com.kibit.paymentapi.dto.TransactionResponseDto;
import com.kibit.paymentapi.exception.IdempotencyKeyExistsException;
import com.kibit.paymentapi.exception.InsufficientBalanceException;
import com.kibit.paymentapi.exception.PaymentProcessingException;
import com.kibit.paymentapi.model.Account;
import com.kibit.paymentapi.model.Transaction;
import com.kibit.paymentapi.repository.AccountRepository;
import com.kibit.paymentapi.repository.TransactionRepository;
import com.kibit.paymentapi.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequestDto request;
    private Account sender;
    private Account recipient;
    private UUID transactionId = UUID.randomUUID();;

    @BeforeEach
    void setUp() {
        this.sender = new Account();
        sender.setAccountNumber("AC123456");
        sender.setBalance(new BigDecimal("1000"));

        this.recipient = new Account();
        recipient.setAccountNumber("AC654321");
        recipient.setBalance(new BigDecimal("500"));

        request = new PaymentRequestDto(transactionId,
                sender.getAccountNumber(),
                recipient.getAccountNumber(),
                BigDecimal.valueOf(100));

    }

    @Test
    void testProcessPayment_SuccessfulTransaction() {
        when(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false);
        when(accountRepository.findByAccountNumberAndLockRow(request.getSenderAccount())).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(request.getRecipientAccount())).thenReturn(Optional.of(recipient));
        when(transactionRepository.save(any())).thenReturn(new Transaction());

        ResponseEntity<TransactionResponseDto> response = paymentService.processPayment(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(BigDecimal.valueOf(900), sender.getBalance());
        assertEquals(BigDecimal.valueOf(600), recipient.getBalance());
        verify(kafkaTemplate).send(any(), any());
    }

    @Test
    void testProcessPayment_IdempotencyKeyExists() {
        when(transactionRepository.existsByIdempotencyKey(any())).thenReturn(true);

        assertThrows(IdempotencyKeyExistsException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void testProcessPayment_NegativeAmount() {
        request.setAmount(BigDecimal.valueOf(-10));

        assertThrows(PaymentProcessingException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void testProcessPayment_ZeroAmount() {
        request.setAmount(BigDecimal.ZERO);

        assertThrows(PaymentProcessingException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void testProcessPayment_SenderNotFound() {
        when(accountRepository.findByAccountNumberAndLockRow(request.getSenderAccount())).thenReturn(Optional.empty());

        assertThrows(PaymentProcessingException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void testProcessPayment_RecipientNotFound() {
        when(accountRepository.findByAccountNumberAndLockRow(request.getSenderAccount())).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(request.getRecipientAccount())).thenReturn(Optional.empty());

        assertThrows(PaymentProcessingException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void testProcessPayment_InsufficientBalance() {
        sender.setBalance(BigDecimal.valueOf(50));
        when(accountRepository.findByAccountNumberAndLockRow(request.getSenderAccount())).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(request.getRecipientAccount())).thenReturn(Optional.of(recipient));

        assertThrows(InsufficientBalanceException.class, () -> paymentService.processPayment(request));
    }

    @Test
    void testProcessPayment_ConcurrentRequests() throws InterruptedException {
        when(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false).thenReturn(true);
        when(accountRepository.findByAccountNumberAndLockRow(request.getSenderAccount())).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(request.getRecipientAccount())).thenReturn(Optional.of(recipient));
        when(transactionRepository.save(any())).thenReturn(new Transaction());


        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 10; i++) {
                executor.execute(() -> {
                    try {
                        paymentService.processPayment(request);
                    } catch (Exception ignored) {
                    }
                });
            }
        }

        assertEquals(BigDecimal.valueOf(900), sender.getBalance());
        assertEquals(BigDecimal.valueOf(600), recipient.getBalance());

        verify(transactionRepository, atLeastOnce()).existsByIdempotencyKey(any());
        verify(accountRepository, atLeastOnce()).findByAccountNumberAndLockRow(any());
        verify(accountRepository, atLeastOnce()).findByAccountNumber(any());
        verify(accountRepository, atLeastOnce()).save(any());
        verify(transactionRepository, atLeastOnce()).save(any());
        verify(kafkaTemplate, atLeastOnce()).send(any(), any());
    }
}