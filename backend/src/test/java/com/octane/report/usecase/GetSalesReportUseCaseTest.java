package com.octane.report.usecase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSalesReportUseCaseTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private GetSalesReportUseCase sut;

    @Test
    void execute_returnsEmptyReport_whenNoData() {
        var stationId = UUID.randomUUID();
        var from = LocalDate.of(2026, 6, 1);
        var to = LocalDate.of(2026, 6, 7);

        var totalsTuple = mock(Tuple.class);
        when(totalsTuple.get("revenue")).thenReturn(BigDecimal.ZERO);
        when(totalsTuple.get("liters")).thenReturn(BigDecimal.ZERO);
        when(totalsTuple.get("cnt")).thenReturn(0L);

        var totalsQuery = mock(Query.class);
        when(totalsQuery.setParameter(anyString(), any())).thenReturn(totalsQuery);
        when(totalsQuery.getSingleResult()).thenReturn(totalsTuple);

        var emptyQuery = mock(Query.class);
        when(emptyQuery.setParameter(anyString(), any())).thenReturn(emptyQuery);
        when(emptyQuery.getResultList()).thenReturn(Collections.emptyList());

        when(em.createNativeQuery(anyString(), eq(Tuple.class)))
                .thenReturn(totalsQuery)
                .thenReturn(emptyQuery)
                .thenReturn(emptyQuery)
                .thenReturn(emptyQuery);

        var result = sut.execute(stationId, from, to);

        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalCount()).isZero();
        assertThat(result.daily()).isEmpty();
        assertThat(result.byFuel()).isEmpty();
        assertThat(result.byPayment()).isEmpty();
    }
}
