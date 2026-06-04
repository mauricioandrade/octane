package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindShiftUseCase {

    private final ShiftRepository shiftRepository;

    public FindShiftUseCase(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Shift execute(UUID id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + id));
    }
}
