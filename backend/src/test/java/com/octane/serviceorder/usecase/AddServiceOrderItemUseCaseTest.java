package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderItem;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddServiceOrderItemUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private ServiceOrderItemRepository serviceOrderItemRepository;

    @InjectMocks
    private AddServiceOrderItemUseCase sut;

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

    private ServiceOrderItem makeItem(UUID id, ServiceOrder order) {
        return new ServiceOrderItem(id, order, "Troca de óleo", new BigDecimal("1.000"),
                new BigDecimal("50.00"), LocalDateTime.now());
    }

    @Test
    void execute_addsItem_whenOrderOpen() {
        var orderId = UUID.randomUUID();
        var order = makeOrder(orderId, ServiceOrderStatus.OPEN);
        var item = makeItem(UUID.randomUUID(), order);
        var request = new AddServiceOrderItemRequest("Troca de óleo",
                new BigDecimal("1.000"), new BigDecimal("50.00"));

        when(serviceOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(serviceOrderItemRepository.save(any(ServiceOrderItem.class))).thenReturn(item);
        when(serviceOrderItemRepository.findByServiceOrderId(orderId)).thenReturn(List.of(item));

        var result = sut.execute(orderId, request);

        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).description()).isEqualTo("Troca de óleo");
        verify(serviceOrderItemRepository).save(any(ServiceOrderItem.class));
    }

    @Test
    void execute_throwsBusinessException_whenOrderNotOpen() {
        var orderId = UUID.randomUUID();
        var order = makeOrder(orderId, ServiceOrderStatus.CLOSED);
        var request = new AddServiceOrderItemRequest("Troca de óleo",
                new BigDecimal("1.000"), new BigDecimal("50.00"));

        when(serviceOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> sut.execute(orderId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está aberta");
    }
}
