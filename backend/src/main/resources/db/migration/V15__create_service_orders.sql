CREATE TABLE service_orders (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id    UUID NOT NULL REFERENCES stations(id),
    plate         VARCHAR(10) NOT NULL,
    odometer      INTEGER NOT NULL,
    customer_name VARCHAR(100),
    customer_phone VARCHAR(20),
    status        VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    notes         VARCHAR(500),
    opened_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at     TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_service_orders_station ON service_orders(station_id);
CREATE INDEX idx_service_orders_plate   ON service_orders(plate);
CREATE INDEX idx_service_orders_status  ON service_orders(status);
