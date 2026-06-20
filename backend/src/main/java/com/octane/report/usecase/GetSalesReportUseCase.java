package com.octane.report.usecase;

import com.octane.report.usecase.SalesReportResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GetSalesReportUseCase {

    private final EntityManager em;

    public GetSalesReportUseCase(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public SalesReportResponse execute(UUID stationId, LocalDate from, LocalDate to) {
        var start = from.atStartOfDay();
        var end = to.plusDays(1).atStartOfDay();

        var totals = (Tuple) em.createNativeQuery("""
            SELECT COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COALESCE(SUM(f.liters), 0) AS liters, COUNT(f.id) AS cnt
            FROM fuelings f JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :sid AND f.fueled_at >= :start AND f.fueled_at < :end
              AND f.status = 'COMPLETED'
            """, Tuple.class)
            .setParameter("sid", stationId).setParameter("start", start).setParameter("end", end)
            .getSingleResult();

        @SuppressWarnings("unchecked")
        List<Tuple> dailyRows = em.createNativeQuery("""
            SELECT CAST(f.fueled_at AS DATE) AS dt,
                   COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COALESCE(SUM(f.liters), 0) AS liters, COUNT(f.id) AS cnt
            FROM fuelings f JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :sid AND f.fueled_at >= :start AND f.fueled_at < :end
              AND f.status = 'COMPLETED'
            GROUP BY CAST(f.fueled_at AS DATE) ORDER BY dt
            """, Tuple.class)
            .setParameter("sid", stationId).setParameter("start", start).setParameter("end", end)
            .getResultList();

        @SuppressWarnings("unchecked")
        List<Tuple> fuelRows = em.createNativeQuery("""
            SELECT f.fuel_name AS fuel, COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COALESCE(SUM(f.liters), 0) AS liters, COUNT(f.id) AS cnt
            FROM fuelings f JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :sid AND f.fueled_at >= :start AND f.fueled_at < :end
              AND f.status = 'COMPLETED'
            GROUP BY f.fuel_name ORDER BY revenue DESC
            """, Tuple.class)
            .setParameter("sid", stationId).setParameter("start", start).setParameter("end", end)
            .getResultList();

        @SuppressWarnings("unchecked")
        List<Tuple> payRows = em.createNativeQuery("""
            SELECT f.payment_method AS method, COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COUNT(f.id) AS cnt
            FROM fuelings f JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :sid AND f.fueled_at >= :start AND f.fueled_at < :end
              AND f.status = 'COMPLETED'
            GROUP BY f.payment_method ORDER BY revenue DESC
            """, Tuple.class)
            .setParameter("sid", stationId).setParameter("start", start).setParameter("end", end)
            .getResultList();

        return new SalesReportResponse(
            (BigDecimal) totals.get("revenue"),
            (BigDecimal) totals.get("liters"),
            ((Number) totals.get("cnt")).longValue(),
            dailyRows.stream().map(r -> new DailySummary(
                ((java.sql.Date) r.get("dt")).toLocalDate(),
                (BigDecimal) r.get("revenue"), (BigDecimal) r.get("liters"),
                ((Number) r.get("cnt")).longValue()
            )).toList(),
            fuelRows.stream().map(r -> new FuelSummary(
                (String) r.get("fuel"), (BigDecimal) r.get("revenue"),
                (BigDecimal) r.get("liters"), ((Number) r.get("cnt")).longValue()
            )).toList(),
            payRows.stream().map(r -> new PaymentSummary(
                (String) r.get("method"), (BigDecimal) r.get("revenue"),
                ((Number) r.get("cnt")).longValue()
            )).toList()
        );
    }
}
