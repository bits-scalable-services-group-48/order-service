package com.ticketing.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {

    private Long ticketId;
    private Long eventId;
    private Long seatId;
    private BigDecimal pricePaid;
}

