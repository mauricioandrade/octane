package com.octane.dashboard.usecase;

import com.octane.dashboard.usecase.DashboardResponse.ActiveShiftInfo;
import com.octane.dashboard.usecase.DashboardResponse.FuelBreakdown;
import com.octane.dashboard.usecase.DashboardResponse.PaymentBreakdown;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GetDashboardUseCase {

    private final EntityManager em;

    public GetDashboardUseCase(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public DashboardResponse execute(UUID stationId) {
        var todayStart = LocalDate.now().atStartOfDay();
        var todayEnd = todayStart.plusDays(1);

        var totals = em.createNativeQuery("""
            SELECT COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COALESCE(SUM(f.liters), 0) AS liters,
                   COUNT(f.id) AS cnt
            FROM fuelings f
            JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :stationId
              AND f.fueled_at >= :start AND f.fueled_at < :end
              AND f.status = 'COMPLETED'
            """, Tuple.class)
            .setParameter("stationId", stationId)
            .setParameter("start", todayStart)
            .setParameter("end", todayEnd)
            .getSingleResult();

        var totalTuple = (Tuple) totals;
        var totalRevenue = (BigDecimal) totalTuple.get("revenue");
        var totalLiters = (BigDecimal) totalTuple.get("liters");
        var fuelingCount = ((Number) totalTuple.get("cnt")).longValue();

        var openSOs = ((Number) em.createNativeQuery("""
            SELECT COUNT(*) FROM service_orders
            WHERE station_id = :stationId AND status = 'OPEN'
            """)
            .setParameter("stationId", stationId)
            .getSingleResult()).longValue();

        var fleetCount = ((Number) em.createNativeQuery("""
            SELECT COUNT(ff.id) FROM fleet_fuelings ff
            JOIN fuelings f ON ff.fueling_id = f.id
            JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :stationId
              AND f.fueled_at >= :start AND f.fueled_at < :end
            """)
            .setParameter("stationId", stationId)
            .setParameter("start", todayStart)
            .setParameter("end", todayEnd)
            .getSingleResult()).longValue();

        ActiveShiftInfo activeShift = null;
        @SuppressWarnings("unchecked")
        var shiftRows = em.createNativeQuery("""
            SELECT employee_name, opened_at FROM shifts
            WHERE station_id = :stationId AND status = 'OPEN'
            LIMIT 1
            """, Tuple.class)
            .setParameter("stationId", stationId)
            .getResultList();
        if (!shiftRows.isEmpty()) {
            var row = (Tuple) shiftRows.get(0);
            activeShift = new ActiveShiftInfo(
                (String) row.get("employee_name"),
                ((java.sql.Timestamp) row.get("opened_at")).toLocalDateTime()
            );
        }

        @SuppressWarnings("unchecked")
        List<Tuple> fuelRows = em.createNativeQuery("""
            SELECT f.fuel_name AS fuel, COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COALESCE(SUM(f.liters), 0) AS liters
            FROM fuelings f
            JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :stationId
              AND f.fueled_at >= :start AND f.fueled_at < :end
              AND f.status = 'COMPLETED'
            GROUP BY f.fuel_name ORDER BY revenue DESC
            """, Tuple.class)
            .setParameter("stationId", stationId)
            .setParameter("start", todayStart)
            .setParameter("end", todayEnd)
            .getResultList();

        var revenueByFuel = fuelRows.stream()
            .map(r -> new FuelBreakdown(
                (String) r.get("fuel"),
                (BigDecimal) r.get("revenue"),
                (BigDecimal) r.get("liters")
            )).toList();

        @SuppressWarnings("unchecked")
        List<Tuple> paymentRows = em.createNativeQuery("""
            SELECT f.payment_method AS method, COALESCE(SUM(f.total_amount), 0) AS revenue,
                   COUNT(f.id) AS cnt
            FROM fuelings f
            JOIN shifts s ON f.shift_id = s.id
            WHERE s.station_id = :stationId
              AND f.fueled_at >= :start AND f.fueled_at < :end
              AND f.status = 'COMPLETED'
            GROUP BY f.payment_method ORDER BY revenue DESC
            """, Tuple.class)
            .setParameter("stationId", stationId)
            .setParameter("start", todayStart)
            .setParameter("end", todayEnd)
            .getResultList();

        var revenueByPayment = paymentRows.stream()
            .map(r -> new PaymentBreakdown(
                (String) r.get("method"),
                (BigDecimal) r.get("revenue"),
                ((Number) r.get("cnt")).longValue()
            )).toList();

        return new DashboardResponse(
            totalRevenue, totalLiters, fuelingCount, openSOs,
            fleetCount, activeShift, revenueByFuel, revenueByPayment
        );
    }
}
