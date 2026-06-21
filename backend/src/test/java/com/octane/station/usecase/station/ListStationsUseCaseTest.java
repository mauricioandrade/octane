package com.octane.station.usecase.station;

import com.octane.shared.auth.AuthenticatedUser;
import com.octane.shared.auth.AuthenticatedUserService;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import com.octane.user.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListStationsUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private ListStationsUseCase sut;

    private Station buildStation(UUID id, String name) {
        return new Station(id, name, "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP",
                true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void admin_returnsAllStations() {
        var s1 = buildStation(UUID.randomUUID(), "Posto X");
        var s2 = buildStation(UUID.randomUUID(), "Posto Y");
        when(stationRepository.findAll((Boolean) null)).thenReturn(List.of(s1, s2));
        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(UUID.randomUUID(), "admin", UserRole.ADMIN, List.of()));

        var result = sut.execute(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void nonAdmin_returnsOnlyAllowedStations() {
        var allowed = UUID.randomUUID();
        var denied = UUID.randomUUID();
        var s1 = buildStation(allowed, "Posto X");
        var s2 = buildStation(denied, "Posto Y");
        when(stationRepository.findAll((Boolean) null)).thenReturn(List.of(s1, s2));
        when(authenticatedUserService.getCurrentUser()).thenReturn(
                new AuthenticatedUser(UUID.randomUUID(), "operator", UserRole.ATTENDANT, List.of(allowed)));

        var result = sut.execute(null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(allowed);
    }
}
