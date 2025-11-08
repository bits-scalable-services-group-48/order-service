package com.ticketing.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long eventId;

    @NotEmpty
    private List<Long> seatIds;

    @NotNull
    private String paymentMethod;   // e.g. CARD, UPI
}
