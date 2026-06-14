CREATE TABLE commission_rules (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id  UUID NOT NULL REFERENCES stations(id),
    employee_name VARCHAR(100) NOT NULL,
    rate        NUMERIC(5,4) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_commission_rule_employee_station UNIQUE (employee_name, station_id)
);
CREATE INDEX idx_commission_rules_station ON commission_rules(station_id);
