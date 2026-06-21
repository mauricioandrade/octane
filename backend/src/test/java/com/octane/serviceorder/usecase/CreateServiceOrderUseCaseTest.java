package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderStatus;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import com.octane.audit.usecase.AuditService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private ServiceOrderItemRepository serviceOrderItemRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CreateServiceOrderUseCase sut;

    private Station makeStation(UUID id, boolean active) {
        var now = LocalDateTime.now();
        return new Station(id, "Posto A", "00.000.000/0001-00", "Rua X, 1", "Curitiba", "PR", active, now, now);
    }

    private ServiceOrder makeOrder(UUID id, Station station) {
        var now = LocalDateTime.now();
        return new ServiceOrder(id, station, "ABC1234", 50000,
                null, null, ServiceOrderStatus.OPEN, null, now, null, now);
    }

    @Test
    void execute_createsOrder_withValidRequest() {
        var stationId = UUID.randomUUID();
        var station = makeStation(stationId, true);
        var request = new CreateServiceOrderRequest(stationId, "ABC1234", 50000, null, null, null);
        var savedOrder = makeOrder(UUID.randomUUID(), station);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(savedOrder);

        var result = sut.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.plate()).isEqualTo("ABC1234");
        assertThat(result.status()).isEqualTo("OPEN");
        assertThat(result.items()).isEmpty();
        verify(serviceOrderRepository).save(any(ServiceOrder.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenStationNotFound() {
        var stationId = UUID.randomUUID();
        var request = new CreateServiceOrderRequest(stationId, "ABC1234", 50000, null, null, null);

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(stationId.toString());

        verify(serviceOrderRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenStationInactive() {
        var stationId = UUID.randomUUID();
        var station = makeStation(stationId, false);
        var request = new CreateServiceOrderRequest(stationId, "ABC1234", 50000, null, null, null);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inativo");

        verify(serviceOrderRepository, never()).save(any());
    }
}
