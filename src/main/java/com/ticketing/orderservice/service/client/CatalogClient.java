package com.ticketing.orderservice.service.client;

import com.ticketing.orderservice.dto.CatalogEventDto;
import com.ticketing.orderservice.dto.CatalogSeatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogClient {

    private final RestTemplate restTemplate;

    @Value("${services.catalog-url:http://localhost:8081}")
    private String catalogBaseUrl;

    public CatalogEventDto getEvent(Long eventId) {
        String url = catalogBaseUrl + "/v1/events/" + eventId;
        return restTemplate.getForObject(url, CatalogEventDto.class);
    }

    public List<CatalogSeatDto> getSeatsForEvent(Long eventId) {
        String url = catalogBaseUrl + "/v1/events/" + eventId + "/seats";
        CatalogSeatDto[] seats = restTemplate.getForObject(url, CatalogSeatDto[].class);
        return seats != null ? Arrays.asList(seats) : List.of();
    }
}

