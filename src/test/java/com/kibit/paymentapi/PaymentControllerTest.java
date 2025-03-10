package com.kibit.paymentapi;

import com.kibit.paymentapi.controller.PaymentController;
import com.kibit.paymentapi.dto.PaymentRequestDto;
import com.kibit.paymentapi.dto.TransactionResponseDto;
import com.kibit.paymentapi.service.PaymentService;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void testProcessPayment() {
        PaymentRequestDto request = new PaymentRequestDto();
        TransactionResponseDto response = new TransactionResponseDto(true, "SUCCESS");
        when(paymentService.processPayment(any(PaymentRequestDto.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        ResponseEntity<TransactionResponseDto> result = paymentController.processPayment(request);

        assertNotNull(result);
        assertThat(HttpStatus.OK).isEqualTo(result.getStatusCode());
        assertThat("SUCCESS").isEqualTo(Objects.requireNonNull(result.getBody()).getMessage());
    }

}
