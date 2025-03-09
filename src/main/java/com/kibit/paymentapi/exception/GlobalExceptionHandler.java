package com.kibit.paymentapi.exception;

import com.kibit.paymentapi.dto.TransactionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.kibit.paymentapi.util.PaymentUtil.createResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<TransactionResponseDto> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return createResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<TransactionResponseDto> handlePaymentProcessingException(PaymentProcessingException ex) {
        return createResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(IdempotencyKeyExistsException.class)
    public ResponseEntity<TransactionResponseDto> handleIdempotencyKeyExistsException(IdempotencyKeyExistsException ex) {
        return createResponse(false, HttpStatus.CONFLICT, ex.getMessage());
    }
}
