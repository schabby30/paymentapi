package com.kibit.paymentapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentRequestDto {
    private UUID idempotencyKey;
    private String senderAccount;
    private String recipientAccount;
    private BigDecimal amount;
}
