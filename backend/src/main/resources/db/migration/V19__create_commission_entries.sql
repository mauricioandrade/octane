CREATE TABLE commission_entries (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id      UUID NOT NULL REFERENCES shifts(id),
    employee_name VARCHAR(100) NOT NULL,
    station_id    UUID NOT NULL REFERENCES stations(id),
    base_amount   NUMERIC(12,2) NOT NULL,
    rate          NUMERIC(5,4) NOT NULL,
    commission    NUMERIC(12,2) NOT NULL,
    paid          BOOLEAN NOT NULL DEFAULT FALSE,
    paid_at       TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_commission_entry_shift UNIQUE (shift_id)
);
CREATE INDEX idx_commission_entries_station   ON commission_entries(station_id);
CREATE INDEX idx_commission_entries_employee  ON commission_entries(employee_name);
CREATE INDEX idx_commission_entries_paid      ON commission_entries(paid);
