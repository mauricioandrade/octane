CREATE TABLE nozzles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number INTEGER NOT NULL,
    pump_id UUID NOT NULL REFERENCES pumps(id),
    fuel_id UUID NOT NULL REFERENCES fuels(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(pump_id, number)
);
