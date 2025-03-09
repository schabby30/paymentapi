package com.kibit.paymentapi.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TransactionResponseDto {

    private Boolean success;
    private String message;

}
