CREATE TABLE tanks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL REFERENCES stations(id),
    fuel_id UUID NOT NULL REFERENCES fuels(id),
    name VARCHAR(50) NOT NULL,
    capacity NUMERIC(12,2) NOT NULL,
    current_level NUMERIC(12,2) NOT NULL DEFAULT 0,
    minimum_level NUMERIC(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tank_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tank_id UUID NOT NULL REFERENCES tanks(id),
    type VARCHAR(20) NOT NULL,
    volume_liters NUMERIC(12,3) NOT NULL,
    previous_level NUMERIC(12,2) NOT NULL,
    new_level NUMERIC(12,2) NOT NULL,
    reference_id UUID,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
