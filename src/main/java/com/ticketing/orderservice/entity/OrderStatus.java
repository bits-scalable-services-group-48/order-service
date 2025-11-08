package com.ticketing.orderservice.entity;


public enum OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    PAYMENT_FAILED
}
