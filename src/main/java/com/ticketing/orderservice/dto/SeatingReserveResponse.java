package com.ticketing.orderservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingReserveResponse {

    private Long eventId;
    private String orderReference;
    private List<Long> reservedSeatIds;
    private LocalDateTime expiresAt;
}

