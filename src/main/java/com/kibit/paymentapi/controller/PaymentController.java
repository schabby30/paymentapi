package com.kibit.paymentapi.controller;

import com.kibit.paymentapi.dto.PaymentRequestDto;
import com.kibit.paymentapi.dto.TransactionResponseDto;
import com.kibit.paymentapi.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/process_payment")
    public ResponseEntity<TransactionResponseDto> processPayment(@RequestBody PaymentRequestDto request) {
        return paymentService.processPayment(request);
    }
}
