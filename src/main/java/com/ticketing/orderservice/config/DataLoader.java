package com.ticketing.orderservice.config;

import com.ticketing.orderservice.entity.Order;
import com.ticketing.orderservice.entity.OrderStatus;
import com.ticketing.orderservice.entity.PaymentStatus;
import com.ticketing.orderservice.entity.Ticket;
import com.ticketing.orderservice.repository.OrderRepository;
import com.ticketing.orderservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // Map CSV order_id -> DB generated order_id
    private final Map<Long, Long> orderIdMap = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (orderRepository.count() > 0) {
            log.info("Data already exists. Skipping seed data loading.");
            return;
        }

        log.info("Starting seed data loading for Order Service...");

        // Note: Users table is created but not loaded from CSV
        // Users will be managed by a separate User Service or replicated as needed
        
    loadOrders();
    loadTickets();

        log.info("Seed data loading completed successfully!");
    }

    private void loadOrders() throws Exception {
        log.info("Loading orders from CSV...");
        var resource = new ClassPathResource("seed-data/etsr_orders.csv");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line = reader.readLine(); // Skip header
            int count = 0;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                Long csvOrderId = Long.parseLong(fields[0]);

                // Do not set the ID; let DB generate it to avoid merge/stale object issues
                Order order = Order.builder()
                        .userId(Long.parseLong(fields[1]))
                        .eventId(Long.parseLong(fields[2]))
                        .status(OrderStatus.valueOf(fields[3]))
                        .paymentStatus(PaymentStatus.valueOf(fields[4]))
                        .orderTotal(new BigDecimal(fields[5]))
                        .createdAt(LocalDateTime.parse(fields[6], FORMATTER))
                        .build();

                order = orderRepository.save(order);
                // Track mapping from CSV id to generated DB id
                orderIdMap.put(csvOrderId, order.getId());
                count++;
            }

            log.info("Loaded {} orders", count);
        }
    }

    private void loadTickets() throws Exception {
        log.info("Loading tickets from CSV...");
        var resource = new ClassPathResource("seed-data/etsr_tickets.csv");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line = reader.readLine(); // Skip header
            int count = 0;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                Long csvOrderId = Long.parseLong(fields[1]);
                Long dbOrderId = orderIdMap.get(csvOrderId);
                if (dbOrderId == null) {
                    throw new RuntimeException("Order mapping not found for CSV order_id=" + csvOrderId);
                }
                Order order = orderRepository.findById(dbOrderId)
                        .orElseThrow(() -> new RuntimeException("Order not found by DB id: " + dbOrderId));

                // Do not set ticket_id; let DB generate
                Ticket ticket = Ticket.builder()
                        .order(order)
                        .eventId(Long.parseLong(fields[2]))
                        .seatId(Long.parseLong(fields[3]))
                        .pricePaid(new BigDecimal(fields[4]))
                        .build();

                ticketRepository.save(ticket);
                count++;
            }

            log.info("Loaded {} tickets", count);
        }
    }
}
