package com.octane.station.usecase.nozzle;

import java.util.UUID;

public record UpdateNozzleRequest(int number, UUID fuelId) {}
