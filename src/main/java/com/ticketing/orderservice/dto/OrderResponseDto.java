package com.ticketing.orderservice.dto;

import com.ticketing.orderservice.entity.OrderStatus;
import com.ticketing.orderservice.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long orderId;
    private Long userId;
    private Long eventId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private List<TicketDto> tickets;

    private LocalDateTime createdAt;
}

