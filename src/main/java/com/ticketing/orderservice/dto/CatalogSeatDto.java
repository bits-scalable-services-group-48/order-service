package com.ticketing.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogSeatDto {

    private Long id;
    private Long eventId;
    private String section;
    private Integer rowNumber;
    private Integer seatNumber;
    private BigDecimal price;
}

