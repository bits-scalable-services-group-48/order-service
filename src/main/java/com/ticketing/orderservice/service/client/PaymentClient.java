package com.ticketing.orderservice.service.client;

import com.ticketing.orderservice.dto.PaymentChargeRequest;
import com.ticketing.orderservice.dto.PaymentChargeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestTemplate restTemplate;

    @Value("${services.payment-url}")
    private String paymentBaseUrl;

    public PaymentChargeResponse charge(String idempotencyKey, PaymentChargeRequest request) {
        String url = paymentBaseUrl + "/v1/payments/charge";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);

        HttpEntity<PaymentChargeRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentChargeResponse> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, PaymentChargeResponse.class);

        return response.getBody();
    }
}

