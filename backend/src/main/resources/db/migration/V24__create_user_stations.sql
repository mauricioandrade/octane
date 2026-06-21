CREATE TABLE user_stations (
    user_id UUID NOT NULL REFERENCES users(id),
    station_id UUID NOT NULL REFERENCES stations(id),
    PRIMARY KEY (user_id, station_id)
);

INSERT INTO user_stations (user_id, station_id)
SELECT u.id, s.id FROM users u, stations s WHERE u.role = 'ADMIN';
