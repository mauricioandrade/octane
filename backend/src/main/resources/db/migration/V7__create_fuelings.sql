CREATE TABLE fuelings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id UUID NOT NULL REFERENCES shifts(id),
    nozzle_id UUID NOT NULL REFERENCES nozzles(id),
    liters NUMERIC(10,3) NOT NULL,
    unit_price NUMERIC(10,4) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    vehicle_plate VARCHAR(10),
    notes VARCHAR(300),
    fueled_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
