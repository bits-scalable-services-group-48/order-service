package com.ticketing.orderservice.dto;

import com.ticketing.orderservice.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogEventDto {

    private Long id;
    private String title;
    private String eventType;
    private String status;          // "ON_SALE", "CANCELLED", ...
    private LocalDateTime eventDate;
    private BigDecimal basePrice;
    private Long venueId;
    private String venueName;
    private String venueCity;
}
