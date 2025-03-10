package com.kibit.paymentapi.exception;

public class KafkaException extends RuntimeException {
    public KafkaException(String message) {
        super(message);
    }
}
