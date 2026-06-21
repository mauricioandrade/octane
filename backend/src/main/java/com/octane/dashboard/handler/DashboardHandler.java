package com.octane.dashboard.handler;

import com.octane.dashboard.usecase.DashboardResponse;
import com.octane.dashboard.usecase.GetDashboardUseCase;
import com.octane.shared.auth.AuthenticatedUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DashboardHandler {

    private final GetDashboardUseCase getDashboardUseCase;
    private final AuthenticatedUserService authService;

    public DashboardHandler(GetDashboardUseCase getDashboardUseCase,
                            AuthenticatedUserService authService) {
        this.getDashboardUseCase = getDashboardUseCase;
        this.authService = authService;
    }

    @GetMapping("/api/dashboard")
    public DashboardResponse getDashboard(@RequestParam UUID stationId) {
        authService.validateStationAccess(stationId);
        return getDashboardUseCase.execute(stationId);
    }
}
