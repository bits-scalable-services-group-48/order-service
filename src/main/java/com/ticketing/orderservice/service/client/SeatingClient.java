package com.ticketing.orderservice.service.client;


import com.ticketing.orderservice.dto.SeatingReleaseOrAllocateRequest;
import com.ticketing.orderservice.dto.SeatingReserveRequest;
import com.ticketing.orderservice.dto.SeatingReserveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SeatingClient {

    private final RestTemplate restTemplate;

    @Value("${services.seating-url}")
    private String seatingBaseUrl;

    public SeatingReserveResponse reserveSeats(SeatingReserveRequest request) {
        String url = seatingBaseUrl + "/v1/seats/reserve";
        return restTemplate.postForObject(url, request, SeatingReserveResponse.class);
    }

    public void releaseSeats(SeatingReleaseOrAllocateRequest request) {
        String url = seatingBaseUrl + "/v1/seats/release";
        restTemplate.postForLocation(url, request);
    }

    public void allocateSeats(SeatingReleaseOrAllocateRequest request) {
        String url = seatingBaseUrl + "/v1/seats/allocate";
        restTemplate.postForLocation(url, request);
    }
}
