package com.octane.dashboard.usecase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDashboardUseCaseTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private GetDashboardUseCase sut;

    @Test
    void execute_returnsDashboard_withZeroData() {
        var stationId = UUID.randomUUID();

        var totalsTuple = mock(Tuple.class);
        when(totalsTuple.get("revenue")).thenReturn(BigDecimal.ZERO);
        when(totalsTuple.get("liters")).thenReturn(BigDecimal.ZERO);
        when(totalsTuple.get("cnt")).thenReturn(0L);

        var totalsQuery = mock(Query.class);
        when(totalsQuery.setParameter(anyString(), any())).thenReturn(totalsQuery);
        when(totalsQuery.getSingleResult()).thenReturn(totalsTuple);

        var openSOsQuery = mock(Query.class);
        when(openSOsQuery.setParameter(anyString(), any())).thenReturn(openSOsQuery);
        when(openSOsQuery.getSingleResult()).thenReturn(0L);

        var fleetQuery = mock(Query.class);
        when(fleetQuery.setParameter(anyString(), any())).thenReturn(fleetQuery);
        when(fleetQuery.getSingleResult()).thenReturn(0L);

        var shiftQuery = mock(Query.class);
        when(shiftQuery.setParameter(anyString(), any())).thenReturn(shiftQuery);
        when(shiftQuery.getResultList()).thenReturn(Collections.emptyList());

        var fuelQuery = mock(Query.class);
        when(fuelQuery.setParameter(anyString(), any())).thenReturn(fuelQuery);
        when(fuelQuery.getResultList()).thenReturn(Collections.emptyList());

        var paymentQuery = mock(Query.class);
        when(paymentQuery.setParameter(anyString(), any())).thenReturn(paymentQuery);
        when(paymentQuery.getResultList()).thenReturn(Collections.emptyList());

        when(em.createNativeQuery(anyString(), eq(Tuple.class)))
                .thenReturn(totalsQuery)
                .thenReturn(shiftQuery)
                .thenReturn(fuelQuery)
                .thenReturn(paymentQuery);

        when(em.createNativeQuery(anyString()))
                .thenReturn(openSOsQuery)
                .thenReturn(fleetQuery);

        var result = sut.execute(stationId);

        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalLiters()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.fuelingCount()).isZero();
        assertThat(result.activeShift()).isNull();
        assertThat(result.revenueByFuel()).isEmpty();
        assertThat(result.revenueByPayment()).isEmpty();
    }
}
