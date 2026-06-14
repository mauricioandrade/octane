CREATE TABLE fleet_fuelings (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fueling_id        UUID NOT NULL REFERENCES fuelings(id),
    driver_id         UUID NOT NULL REFERENCES fleet_drivers(id),
    vehicle_id        UUID NOT NULL REFERENCES fleet_vehicles(id),
    odometer          INTEGER NOT NULL,
    previous_odometer INTEGER,
    odometer_alert    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fleet_fueling_fueling UNIQUE (fueling_id)
);
CREATE INDEX idx_fleet_fuelings_driver  ON fleet_fuelings(driver_id);
CREATE INDEX idx_fleet_fuelings_vehicle ON fleet_fuelings(vehicle_id);
CREATE INDEX idx_fleet_fuelings_created ON fleet_fuelings(created_at);
