package com.octane.commission.usecase.entry;

import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.commission.usecase.CommissionEntryResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetShiftCommissionUseCase {

    private final CommissionEntryRepository commissionEntryRepository;

    public GetShiftCommissionUseCase(CommissionEntryRepository commissionEntryRepository) {
        this.commissionEntryRepository = commissionEntryRepository;
    }

    public Optional<CommissionEntryResponse> execute(UUID shiftId) {
        return commissionEntryRepository.findByShiftId(shiftId)
                .map(CommissionEntryResponse::from);
    }
}
