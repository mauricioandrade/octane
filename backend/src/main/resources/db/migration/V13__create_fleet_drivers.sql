CREATE TABLE fleet_drivers (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id  UUID NOT NULL REFERENCES fleet_clients(id),
    name       VARCHAR(100) NOT NULL,
    cpf        VARCHAR(14) NOT NULL,
    pin_hash   VARCHAR(60),
    rfid_tag   VARCHAR(50),
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fleet_driver_cpf_client UNIQUE (cpf, client_id),
    CONSTRAINT chk_fleet_driver_auth CHECK (pin_hash IS NOT NULL OR rfid_tag IS NOT NULL)
);
CREATE INDEX idx_fleet_drivers_client ON fleet_drivers(client_id);
CREATE INDEX idx_fleet_drivers_rfid ON fleet_drivers(rfid_tag) WHERE rfid_tag IS NOT NULL;
