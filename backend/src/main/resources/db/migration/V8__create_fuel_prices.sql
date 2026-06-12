CREATE TABLE fuel_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL REFERENCES stations(id),
    fuel_id UUID NOT NULL REFERENCES fuels(id),
    price NUMERIC(10,4) NOT NULL,
    effective_from TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fuel_prices_lookup
    ON fuel_prices (station_id, fuel_id, effective_from DESC);
