package com.ticketing.orderservice.service;

import com.ticketing.orderservice.dto.*;
import com.ticketing.orderservice.entity.*;
import com.ticketing.orderservice.repository.OrderRepository;
import com.ticketing.orderservice.repository.TicketRepository;
import com.ticketing.orderservice.service.client.CatalogClient;
import com.ticketing.orderservice.service.client.PaymentClient;
import com.ticketing.orderservice.service.client.SeatingClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final CatalogClient catalogClient;
    private final SeatingClient seatingClient;
    private final PaymentClient paymentClient;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05"); // 5%

    public OrderResponseDto createOrder(CreateOrderRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        // Idempotency: return existing order if already processed
        Optional<Order> existingOrderOpt = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrderOpt.isPresent()) {
            Order existing = existingOrderOpt.get();
            List<Ticket> tickets = ticketRepository.findByOrder_Id(existing.getId());
            return toOrderResponse(existing, tickets);
        }

        // 1) Get event info & validate event status
        CatalogEventDto event = catalogClient.getEvent(request.getEventId());
        if (event == null) {
            throw new IllegalStateException("Event not found: " + request.getEventId());
        }
        if (!"ON_SALE".equalsIgnoreCase(event.getStatus())) {
            throw new IllegalStateException("Event is not ON_SALE. Current status = " + event.getStatus());
        }

        // 2) Get seat prices from Catalog
        List<CatalogSeatDto> allSeats = catalogClient.getSeatsForEvent(request.getEventId());
        Map<Long, CatalogSeatDto> seatById = allSeats.stream()
                .collect(Collectors.toMap(CatalogSeatDto::getId, s -> s));

        // Validate requested seats exist
        List<Long> missingSeats = request.getSeatIds().stream()
                .filter(id -> !seatById.containsKey(id))
                .toList();
        if (!missingSeats.isEmpty()) {
            throw new IllegalStateException("Invalid seat ids: " + missingSeats);
        }

        // Compute subtotal
        BigDecimal subtotal = request.getSeatIds().stream()
                .map(seatId -> seatById.get(seatId).getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        // 3) Reserve seats via Seating Service
        SeatingReserveRequest reserveRequest = SeatingReserveRequest.builder()
                .eventId(request.getEventId())
                .seatIds(request.getSeatIds())
                .orderReference(idempotencyKey) // use idempotency key to correlate
                .ttlMinutes(15)
                .build();

        seatingClient.reserveSeats(reserveRequest);

        // 4) Create order with PENDING_PAYMENT
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentStatus(PaymentStatus.PENDING)
                .orderTotal(total)
                .idempotencyKey(idempotencyKey)
                .createdAt(now)
                .build();

        order = orderRepository.save(order);

        // 5) Call Payment Service
        PaymentChargeRequest chargeRequest = PaymentChargeRequest.builder()
                .orderId(order.getId())
                .amount(total)
                .method(request.getPaymentMethod())
                .currency("INR")
                .build();

        PaymentChargeResponse chargeResponse = paymentClient.charge(idempotencyKey, chargeRequest);

        if (chargeResponse == null || chargeResponse.getStatus() == null) {
            // Treat as failed
            handlePaymentFailure(order, request, idempotencyKey);
            throw new IllegalStateException("Payment failed or no response from payment service");
        }

        String paymentStatus = chargeResponse.getStatus().toUpperCase(Locale.ROOT);

        if ("SUCCESS".equals(paymentStatus)) {
            // Payment success
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            orderRepository.save(order);

            // Seating allocate
            SeatingReleaseOrAllocateRequest allocateRequest = SeatingReleaseOrAllocateRequest.builder()
                    .eventId(request.getEventId())
                    .orderReference(idempotencyKey)
                    .seatIds(request.getSeatIds())
                    .build();
            seatingClient.allocateSeats(allocateRequest);

            // Generate tickets
            List<Ticket> tickets = createTickets(order, request.getSeatIds(), seatById);
            return toOrderResponse(order, tickets);
        } else {
            // Payment failed
            handlePaymentFailure(order, request, idempotencyKey);
            throw new IllegalStateException("Payment failed with status: " + paymentStatus);
        }
    }

    private void handlePaymentFailure(Order order, CreateOrderRequest request, String orderReference) {
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);

        // Release seats
        SeatingReleaseOrAllocateRequest releaseRequest = SeatingReleaseOrAllocateRequest.builder()
                .eventId(request.getEventId())
                .orderReference(orderReference)
                .seatIds(request.getSeatIds())
                .build();
        seatingClient.releaseSeats(releaseRequest);
    }

    private List<Ticket> createTickets(Order order, List<Long> seatIds, Map<Long, CatalogSeatDto> seatById) {
        List<Ticket> tickets = seatIds.stream()
                .map(seatId -> Ticket.builder()
                        .order(order)
                        .eventId(order.getEventId())
                        .seatId(seatId)
                        .pricePaid(seatById.get(seatId).getPrice())
                        .build())
                .toList();
        return ticketRepository.saveAll(tickets);
    }

    public OrderResponseDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        List<Ticket> tickets = ticketRepository.findByOrder_Id(orderId);
        return toOrderResponse(order, tickets);
    }

    public List<OrderResponseDto> getOrdersForUser(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(order -> {
                    List<Ticket> tickets = ticketRepository.findByOrder_Id(order.getId());
                    return toOrderResponse(order, tickets);
                })
                .toList();
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        // TODO: call Payment Service refund endpoint here (later)
        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        orderRepository.save(order);

        // Release seats
        SeatingReleaseOrAllocateRequest releaseRequest = SeatingReleaseOrAllocateRequest.builder()
                .eventId(order.getEventId())
                .orderReference(order.getIdempotencyKey())
                .seatIds(null)  // release all seats for this order
                .build();
        seatingClient.releaseSeats(releaseRequest);
    }

    private OrderResponseDto toOrderResponse(Order order, List<Ticket> tickets) {
        BigDecimal subtotal = tickets.stream()
                .map(Ticket::getPricePaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        List<TicketDto> ticketDtos = tickets.stream()
                .map(t -> TicketDto.builder()
                        .ticketId(t.getId())
                        .eventId(t.getEventId())
                        .seatId(t.getSeatId())
                        .pricePaid(t.getPricePaid())
                        .build())
                .toList();

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .eventId(order.getEventId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(subtotal)
                .taxAmount(tax)
                .totalAmount(total)
                .tickets(ticketDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }
}

