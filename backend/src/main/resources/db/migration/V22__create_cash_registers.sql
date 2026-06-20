CREATE TABLE cash_registers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL REFERENCES stations(id),
    shift_id UUID REFERENCES shifts(id),
    opened_at TIMESTAMP NOT NULL DEFAULT now(),
    closed_at TIMESTAMP,
    opening_balance NUMERIC(12,2) NOT NULL,
    closing_balance NUMERIC(12,2),
    status VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE cash_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cash_register_id UUID NOT NULL REFERENCES cash_registers(id),
    type VARCHAR(10) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(200),
    amount NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(20),
    reference_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
