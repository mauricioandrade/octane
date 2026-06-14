CREATE TABLE fleet_vehicles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID NOT NULL REFERENCES fleet_clients(id),
    plate           VARCHAR(10) NOT NULL,
    model           VARCHAR(100),
    allowed_fuel_id UUID NOT NULL REFERENCES fuels(id),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fleet_vehicle_plate UNIQUE (plate)
);
CREATE INDEX idx_fleet_vehicles_client ON fleet_vehicles(client_id);
CREATE INDEX idx_fleet_vehicles_plate ON fleet_vehicles(plate);
