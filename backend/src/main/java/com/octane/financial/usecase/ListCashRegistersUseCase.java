package com.octane.financial.usecase;

import com.octane.financial.domain.repository.CashRegisterRepository;
import com.octane.shared.pagination.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListCashRegistersUseCase {

    private final CashRegisterRepository cashRegisterRepository;

    public ListCashRegistersUseCase(CashRegisterRepository cashRegisterRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CashRegisterResponse> execute(UUID stationId, int page, int size) {
        var springPage = cashRegisterRepository.findClosedByStationId(stationId, PageRequest.of(page, size));
        return PageResponse.of(
            springPage.getContent().stream().map(CashRegisterResponse::from).toList(),
            page, size, springPage.getTotalElements()
        );
    }
}
