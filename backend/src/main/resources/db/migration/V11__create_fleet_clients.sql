CREATE TABLE fleet_clients (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id    UUID NOT NULL REFERENCES stations(id),
    cnpj          VARCHAR(18) NOT NULL,
    company_name  VARCHAR(150) NOT NULL,
    trade_name    VARCHAR(100),
    monthly_limit NUMERIC(12,2),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fleet_client_cnpj_station UNIQUE (cnpj, station_id)
);
CREATE INDEX idx_fleet_clients_station ON fleet_clients(station_id);
