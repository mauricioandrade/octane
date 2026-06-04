package com.octane.station.usecase.nozzle;

import java.util.UUID;

public record CreateNozzleRequest(int number, UUID fuelId) {}
