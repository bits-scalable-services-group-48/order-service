-- Order Service Schema

-- Users table (replicated from catalog-service for order management)
CREATE TABLE IF NOT EXISTS etsr_users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_email ON etsr_users(email);

-- Orders table
CREATE TABLE IF NOT EXISTS etsr_orders (
    order_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    order_total DECIMAL(10, 2) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_user ON etsr_orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_event ON etsr_orders(event_id);
CREATE INDEX IF NOT EXISTS idx_order_idempotency ON etsr_orders(idempotency_key);

-- Tickets table
CREATE TABLE IF NOT EXISTS etsr_tickets (
    ticket_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price_paid DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_ticket_order FOREIGN KEY (order_id) REFERENCES etsr_orders(order_id)
);

CREATE INDEX IF NOT EXISTS idx_ticket_order ON etsr_tickets(order_id);
CREATE INDEX IF NOT EXISTS idx_ticket_event ON etsr_tickets(event_id);
CREATE INDEX IF NOT EXISTS idx_ticket_seat ON etsr_tickets(seat_id);
