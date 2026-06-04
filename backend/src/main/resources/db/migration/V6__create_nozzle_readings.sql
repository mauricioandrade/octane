CREATE TABLE nozzle_readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id UUID NOT NULL REFERENCES shifts(id),
    nozzle_id UUID NOT NULL REFERENCES nozzles(id),
    type VARCHAR(10) NOT NULL, -- OPENING, CLOSING
    totalizer NUMERIC(12,3) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(shift_id, nozzle_id, type)
);
