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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShiftReportUseCaseTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private GetShiftReportUseCase sut;

    @Test
    void execute_returnsEmptyReport_whenNoShifts() {
        var stationId = UUID.randomUUID();
        var from = LocalDate.of(2026, 6, 1);
        var to = LocalDate.of(2026, 6, 7);

        var query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        when(em.createNativeQuery(anyString(), eq(Tuple.class))).thenReturn(query);

        var result = sut.execute(stationId, from, to);

        assertThat(result.shifts()).isEmpty();
        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalLiters()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalFuelings()).isZero();
    }

    @Test
    void execute_returnsShifts_withCorrectTotals() {
        var stationId = UUID.randomUUID();
        var from = LocalDate.of(2026, 6, 1);
        var to = LocalDate.of(2026, 6, 7);

        var openedAt = Timestamp.valueOf(LocalDateTime.of(2026, 6, 1, 8, 0));
        var closedAt = Timestamp.valueOf(LocalDateTime.of(2026, 6, 1, 16, 0));

        var row = mock(Tuple.class);
        when(row.get("employee_name")).thenReturn("João");
        when(row.get("opened_at")).thenReturn(openedAt);
        when(row.get("closed_at")).thenReturn(closedAt);
        when(row.get("revenue")).thenReturn(new BigDecimal("5000.00"));
        when(row.get("liters")).thenReturn(new BigDecimal("1000.000"));
        when(row.get("cnt")).thenReturn(50L);

        var query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(row));

        when(em.createNativeQuery(anyString(), eq(Tuple.class))).thenReturn(query);

        var result = sut.execute(stationId, from, to);

        assertThat(result.shifts()).hasSize(1);
        assertThat(result.shifts().get(0).employeeName()).isEqualTo("João");
        assertThat(result.shifts().get(0).durationMinutes()).isEqualTo(480);
        assertThat(result.totalRevenue()).isEqualByComparingTo("5000.00");
        assertThat(result.totalFuelings()).isEqualTo(50);
    }
}
