package com.octane.report.usecase;

import com.octane.report.usecase.ShiftReportResponse.ShiftLine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GetShiftReportUseCase {

    private final EntityManager em;

    public GetShiftReportUseCase(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public ShiftReportResponse execute(UUID stationId, LocalDate from, LocalDate to) {
        var start = from.atStartOfDay();
        var end = to.plusDays(1).atStartOfDay();

        @SuppressWarnings("unchecked")
        List<Tuple> rows = em.createNativeQuery("""
            SELECT s.employee_name, s.opened_at, s.closed_at,
                   COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COALESCE(SUM(f.liters), 0) AS liters,
                   COUNT(f.id) AS cnt
            FROM shifts s
            LEFT JOIN fuelings f ON f.shift_id = s.id AND f.status = 'COMPLETED'
            WHERE s.station_id = :sid AND s.status = 'CLOSED'
              AND s.opened_at >= :start AND s.opened_at < :end
            GROUP BY s.id, s.employee_name, s.opened_at, s.closed_at
            ORDER BY s.opened_at
            """, Tuple.class)
            .setParameter("sid", stationId)
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList();

        var shifts = rows.stream().map(r -> {
            var openedAt = ((java.sql.Timestamp) r.get("opened_at")).toLocalDateTime();
            var closedAt = ((java.sql.Timestamp) r.get("closed_at")).toLocalDateTime();
            return new ShiftLine(
                (String) r.get("employee_name"),
                openedAt, closedAt,
                Duration.between(openedAt, closedAt).toMinutes(),
                (BigDecimal) r.get("revenue"),
                (BigDecimal) r.get("liters"),
                ((Number) r.get("cnt")).longValue()
            );
        }).toList();

        var totalRevenue = shifts.stream().map(ShiftLine::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalLiters = shifts.stream().map(ShiftLine::liters).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalFuelings = shifts.stream().mapToLong(ShiftLine::fuelingCount).sum();

        return new ShiftReportResponse(shifts, totalRevenue, totalLiters, totalFuelings);
    }
}
