package com.ticketing.orderservice.repository;

import com.ticketing.orderservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByOrder_Id(Long orderId);
}
