ALTER TABLE stations ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE fuels ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE pumps ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE nozzles ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE fleet_clients ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE fleet_vehicles ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE fleet_drivers ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE commission_rules ADD COLUMN deleted_at TIMESTAMP;
