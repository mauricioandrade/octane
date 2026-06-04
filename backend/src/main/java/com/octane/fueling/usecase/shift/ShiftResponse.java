package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShiftResponse(
        UUID id,
        UUID stationId,
        String stationName,
        String employeeName,
        String status,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        String notes
) {
    public static ShiftResponse from(Shift shift) {
        return new ShiftResponse(
                shift.getId(),
                shift.getStation().getId(),
                shift.getStation().getName(),
                shift.getEmployeeName(),
                shift.getStatus().name(),
                shift.getOpenedAt(),
                shift.getClosedAt(),
                shift.getNotes()
        );
    }
}
