CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ATTENDANT',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO users (username, password_hash, name, role)
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMQoeGb.sJkGLkT/r8jKZrKTDAsBUExtTfVjWGm', 'Administrador', 'ADMIN');
