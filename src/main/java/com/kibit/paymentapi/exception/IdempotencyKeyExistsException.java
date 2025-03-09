package com.kibit.paymentapi.exception;

public class IdempotencyKeyExistsException extends RuntimeException {
    public IdempotencyKeyExistsException(String message) {
        super(message);
    }
}
