package com.ticketing.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentChargeRequest {

    private Long orderId;
    private BigDecimal amount;
    private String method;    // CARD, UPI, etc.
    private String currency;  // "INR"
}

