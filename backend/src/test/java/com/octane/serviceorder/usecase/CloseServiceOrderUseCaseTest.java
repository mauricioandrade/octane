package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderStatus;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private ServiceOrderItemRepository serviceOrderItemRepository;

    @InjectMocks
    private CloseServiceOrderUseCase sut;

    private Station makeStation(UUID id) {
        var now = LocalDateTime.now();
        return new Station(id, "Posto A", "00.000.000/0001-00", "Rua X, 1", "Curitiba", "PR", true, now, now);
    }

    private ServiceOrder makeOrder(UUID id, ServiceOrderStatus status) {
        var station = makeStation(UUID.randomUUID());
        var now = LocalDateTime.now();
        return new ServiceOrder(id, station, "ABC1234", 50000,
                null, null, status, null, now, null, now);
    }

    @Test
    void execute_closesOrder_whenOpen() {
        var orderId = UUID.randomUUID();
        var order = makeOrder(orderId, ServiceOrderStatus.OPEN);
        var closedOrder = makeOrder(orderId, ServiceOrderStatus.CLOSED);

        when(serviceOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenReturn(closedOrder);
        when(serviceOrderItemRepository.findByServiceOrderId(orderId)).thenReturn(List.of());

        var result = sut.execute(orderId);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("CLOSED");
    }

    @Test
    void execute_throwsBusinessException_whenAlreadyClosed() {
        var orderId = UUID.randomUUID();
        var order = makeOrder(orderId, ServiceOrderStatus.CLOSED);

        when(serviceOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> sut.execute(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("encerrada ou cancelada");
    }
}
