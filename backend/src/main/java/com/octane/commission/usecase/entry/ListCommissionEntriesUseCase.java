package com.octane.commission.usecase.entry;

import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.commission.usecase.CommissionEntryResponse;
import com.octane.shared.pagination.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ListCommissionEntriesUseCase {

    private final CommissionEntryRepository commissionEntryRepository;

    public ListCommissionEntriesUseCase(CommissionEntryRepository commissionEntryRepository) {
        this.commissionEntryRepository = commissionEntryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CommissionEntryResponse> execute(UUID stationId, Boolean paid,
                                                          LocalDate from, LocalDate to,
                                                          int page, int size) {
        return commissionEntryRepository.findByStationId(stationId, paid, from, to, page, size)
                .map(CommissionEntryResponse::from);
    }
}
