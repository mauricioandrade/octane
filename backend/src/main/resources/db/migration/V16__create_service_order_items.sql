CREATE TABLE service_order_items (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id),
    description      VARCHAR(200) NOT NULL,
    quantity         NUMERIC(8,3) NOT NULL DEFAULT 1,
    unit_price       NUMERIC(10,2) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_service_order_items_order ON service_order_items(service_order_id);
