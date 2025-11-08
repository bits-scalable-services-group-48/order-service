package com.ticketing.orderservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingReserveRequest {

    private Long eventId;
    private List<Long> seatIds;
    private String orderReference;
    private Integer ttlMinutes;
}
