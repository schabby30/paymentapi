package com.kibit.paymentapi.util;

import com.kibit.paymentapi.dto.TransactionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PaymentUtil {

    public static ResponseEntity<TransactionResponseDto> createResponse(Boolean success, HttpStatus status, String message) {
        TransactionResponseDto responseBody = TransactionResponseDto.builder()
                .success(success)
                .message(message)
                .build();

        return new ResponseEntity<>(responseBody, status);
    }

}
