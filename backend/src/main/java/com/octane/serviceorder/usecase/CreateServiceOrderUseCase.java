package com.octane.serviceorder.usecase;

import com.octane.audit.usecase.AuditService;
import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderStatus;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreateServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final StationRepository stationRepository;
    private final AuditService auditService;

    public CreateServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository,
                                     ServiceOrderItemRepository serviceOrderItemRepository,
                                     StationRepository stationRepository,
                                     AuditService auditService) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
        this.stationRepository = stationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ServiceOrderResponse execute(CreateServiceOrderRequest request) {
        var stationId = request.stationId();
        var station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + stationId));

        if (!station.isActive()) {
            throw new BusinessException("Posto inativo");
        }

        var now = LocalDateTime.now();
        var order = new ServiceOrder(null, station, request.plate(), request.odometer(),
                request.customerName(), request.customerPhone(),
                ServiceOrderStatus.OPEN, request.notes(), now, null, now);

        var saved = serviceOrderRepository.save(order);
        auditService.log("CREATE", "ServiceOrder", saved.getId(), saved.getPlate());
        return ServiceOrderResponse.from(saved, List.of());
    }
}
