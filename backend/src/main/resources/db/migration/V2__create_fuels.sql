CREATE TABLE fuels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    unit VARCHAR(10) NOT NULL DEFAULT 'LITER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO fuels (name, unit) VALUES
    ('Gasolina Comum', 'LITER'),
    ('Gasolina Aditivada', 'LITER'),
    ('Etanol', 'LITER'),
    ('Diesel S10', 'LITER'),
    ('Diesel S500', 'LITER');
