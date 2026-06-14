package com.octane.commission.usecase.entry;

import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.commission.usecase.CommissionEntryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ListCommissionEntriesUseCase {

    private final CommissionEntryRepository commissionEntryRepository;

    public ListCommissionEntriesUseCase(CommissionEntryRepository commissionEntryRepository) {
        this.commissionEntryRepository = commissionEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<CommissionEntryResponse> execute(UUID stationId, Boolean paid, LocalDate from, LocalDate to) {
        return commissionEntryRepository.findByStationId(stationId, paid, from, to)
                .stream()
                .map(CommissionEntryResponse::from)
                .toList();
    }
}
