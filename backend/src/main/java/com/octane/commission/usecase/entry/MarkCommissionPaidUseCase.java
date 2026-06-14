package com.octane.commission.usecase.entry;

import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.commission.usecase.CommissionEntryResponse;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MarkCommissionPaidUseCase {

    private final CommissionEntryRepository commissionEntryRepository;

    public MarkCommissionPaidUseCase(CommissionEntryRepository commissionEntryRepository) {
        this.commissionEntryRepository = commissionEntryRepository;
    }

    @Transactional
    public CommissionEntryResponse execute(UUID entryId) {
        var entry = commissionEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("CommissionEntry not found: " + entryId));

        if (entry.isPaid()) {
            throw new BusinessException("Comissão já marcada como paga");
        }

        entry.setPaid(true);
        entry.setPaidAt(LocalDateTime.now());

        var saved = commissionEntryRepository.save(entry);
        return CommissionEntryResponse.from(saved);
    }
}
