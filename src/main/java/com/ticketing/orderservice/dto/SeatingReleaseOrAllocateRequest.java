package com.ticketing.orderservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingReleaseOrAllocateRequest {

    private Long eventId;
    private String orderReference;
    private List<Long> seatIds;
}

