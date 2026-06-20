package com.octane.dashboard.handler;

import com.octane.dashboard.usecase.DashboardResponse;
import com.octane.dashboard.usecase.GetDashboardUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DashboardHandler {

    private final GetDashboardUseCase getDashboardUseCase;

    public DashboardHandler(GetDashboardUseCase getDashboardUseCase) {
        this.getDashboardUseCase = getDashboardUseCase;
    }

    @GetMapping("/api/dashboard")
    public DashboardResponse getDashboard(@RequestParam UUID stationId) {
        return getDashboardUseCase.execute(stationId);
    }
}
