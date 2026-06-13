CREATE TABLE shift_reconciliations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id UUID NOT NULL REFERENCES shifts(id),
    nozzle_id UUID NOT NULL REFERENCES nozzles(id),
    opening_totalizer NUMERIC(12,3) NOT NULL,
    closing_totalizer NUMERIC(12,3) NOT NULL,
    measured_liters NUMERIC(12,3) NOT NULL,
    fueled_liters NUMERIC(12,3) NOT NULL,
    divergence_liters NUMERIC(12,3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (shift_id, nozzle_id)
);
