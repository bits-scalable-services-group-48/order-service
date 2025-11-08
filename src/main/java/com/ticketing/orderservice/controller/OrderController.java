package com.ticketing.orderservice.controller;

import com.ticketing.orderservice.dto.CreateOrderRequest;
import com.ticketing.orderservice.dto.OrderResponseDto;
import com.ticketing.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Place order
     * Requires header: Idempotency-Key
     */
    @PostMapping
    public OrderResponseDto createOrder(
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey,
            @RequestBody @Valid CreateOrderRequest request
    ) {
        return orderService.createOrder(request, idempotencyKey);
    }

    @GetMapping("/{id}")
    public OrderResponseDto getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponseDto> getOrdersForUser(@RequestParam Long userId) {
        return orderService.getOrdersForUser(userId);
    }

    /**
     * Cancel order and release seats (and later trigger refund).
     */
    @PostMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }
}
