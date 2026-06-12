# Núcleo de Pista (Backend) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fechar o domínio de pista do Octane: edição/inativação de cadastros, tabela de preços vigentes, abastecimento com preço da tabela + cancelamento, conciliação de turno e paginação/filtros.

**Architecture:** Clean Architecture já estabelecida no repo — entidades JPA em `domain/`, interfaces puras em `domain/repository/`, use cases `@Service` com construtor explícito, impls `@Repository` delegando para `JpaRepository`, handlers `@RestController`. Novo módulo `com.octane.pricing` no mesmo padrão. Sem Lombok, Records para DTOs, `BigDecimal` para dinheiro/litros.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Flyway, PostgreSQL 16, JUnit 5 + Mockito + AssertJ, MockMvc (`@WebMvcTest` com `@MockitoBean`).

**Spec:** `docs/superpowers/specs/2026-06-12-nucleo-pista-backend-design.md`

**Comandos:**
- Teste único: `cd backend && ./mvnw test -Dtest=NomeDoTeste`
- Suite completa: `cd backend && ./mvnw test`
- Commits seguem `docs/commit-conventions.md`, **sem** `Co-Authored-By`.

**Convenções de teste do repo (seguir à risca):**
- Use case: `@ExtendWith(MockitoExtension.class)`, `@Mock` repos, `@InjectMocks` sut, AssertJ (`assertThat`, `assertThatThrownBy`).
- Handler: `@WebMvcTest(Handler.class)`, `@MockitoBean` use cases, `ObjectMapper` local, import `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` e `org.springframework.test.context.bean.override.mockito.MockitoBean`.
- Nomes: `execute_efeito_quandoCondicao` / `verboRota_retornaXXX`.

---

## Fase A — Edição e inativação de cadastros

### Task A1: UpdateStationUseCase (PUT de posto)

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/station/UpdateStationRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/station/UpdateStationUseCase.java`
- Test: `backend/src/test/java/com/octane/station/usecase/station/UpdateStationUseCaseTest.java`

- [ ] **Step 1: Escrever o teste que falha**

```java
package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStationUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private UpdateStationUseCase sut;

    private Station buildStation(UUID id, String cnpj) {
        return new Station(id, "Posto X", cnpj, "Rua A, 1", "São Paulo", "SP",
            true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void execute_updatesAndReturnsStation_whenFound() {
        var id = UUID.randomUUID();
        var existing = buildStation(id, "12.345.678/0001-90");
        var request = new UpdateStationRequest("Posto Novo", "12.345.678/0001-90", "Rua B, 2", "Campinas", "SP");

        when(stationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(stationRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.of(existing));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, request);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Posto Novo");
        assertThat(result.getCity()).isEqualTo("Campinas");
        assertThat(result.isActive()).isTrue();
        verify(stationRepository).save(any(Station.class));
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var id = UUID.randomUUID();
        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id,
            new UpdateStationRequest("Posto", "12.345.678/0001-90", "Rua", "SP", "SP")))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_throwsBusinessException_whenCnpjBelongsToAnotherStation() {
        var id = UUID.randomUUID();
        var existing = buildStation(id, "12.345.678/0001-90");
        var other = buildStation(UUID.randomUUID(), "99.999.999/0001-99");

        when(stationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(stationRepository.findByCnpj("99.999.999/0001-99")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> sut.execute(id,
            new UpdateStationRequest("Posto", "99.999.999/0001-99", "Rua", "SP", "SP")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CNPJ");

        verify(stationRepository, never()).save(any());
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=UpdateStationUseCaseTest`
Expected: erro de compilação — `UpdateStationRequest`/`UpdateStationUseCase` não existem.

- [ ] **Step 3: Implementar**

`UpdateStationRequest.java`:
```java
package com.octane.station.usecase.station;

public record UpdateStationRequest(
    String name,
    String cnpj,
    String address,
    String city,
    String state
) {}
```

`UpdateStationUseCase.java`:
```java
package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateStationUseCase {

    private final StationRepository stationRepository;

    public UpdateStationUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Station execute(UUID id, UpdateStationRequest request) {
        var station = stationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + id));

        var conflicting = stationRepository.findByCnpj(request.cnpj());
        if (conflicting.isPresent() && !conflicting.get().getId().equals(id)) {
            throw new BusinessException("CNPJ já cadastrado: " + request.cnpj());
        }

        var updated = new Station(station.getId(), request.name(), request.cnpj(),
            request.address(), request.city(), request.state(), station.isActive(),
            station.getCreatedAt(), LocalDateTime.now());
        return stationRepository.save(updated);
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest=UpdateStationUseCaseTest`
Expected: 3 testes PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/station/usecase/station/UpdateStation* backend/src/test/java/com/octane/station/usecase/station/UpdateStationUseCaseTest.java
git commit -m "feat(station): add UpdateStationUseCase"
```

### Task A2: UpdateStationStatusUseCase (ativar/inativar posto)

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/station/UpdateStationStatusRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/station/UpdateStationStatusUseCase.java`
- Test: `backend/src/test/java/com/octane/station/usecase/station/UpdateStationStatusUseCaseTest.java`

- [ ] **Step 1: Escrever o teste que falha**

```java
package com.octane.station.usecase.station;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStationStatusUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private UpdateStationStatusUseCase sut;

    @Test
    void execute_deactivatesStation() {
        var id = UUID.randomUUID();
        var existing = new Station(id, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateStationStatusRequest(false));

        assertThat(result.isActive()).isFalse();
        assertThat(result.getName()).isEqualTo("Posto X");
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var id = UUID.randomUUID();
        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateStationStatusRequest(true)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=UpdateStationStatusUseCaseTest`
Expected: erro de compilação.

- [ ] **Step 3: Implementar**

`UpdateStationStatusRequest.java`:
```java
package com.octane.station.usecase.station;

public record UpdateStationStatusRequest(boolean active) {}
```

`UpdateStationStatusUseCase.java`:
```java
package com.octane.station.usecase.station;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateStationStatusUseCase {

    private final StationRepository stationRepository;

    public UpdateStationStatusUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Station execute(UUID id, UpdateStationStatusRequest request) {
        var station = stationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + id));

        var updated = new Station(station.getId(), station.getName(), station.getCnpj(),
            station.getAddress(), station.getCity(), station.getState(), request.active(),
            station.getCreatedAt(), LocalDateTime.now());
        return stationRepository.save(updated);
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest=UpdateStationStatusUseCaseTest`
Expected: 2 testes PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/station/usecase/station/UpdateStationStatus* backend/src/test/java/com/octane/station/usecase/station/UpdateStationStatusUseCaseTest.java
git commit -m "feat(station): add UpdateStationStatusUseCase"
```

### Task A3: Endpoints PUT /api/stations/{id} e PATCH /api/stations/{id}/status

**Files:**
- Modify: `backend/src/main/java/com/octane/station/handler/StationHandler.java`
- Test: `backend/src/test/java/com/octane/station/handler/StationHandlerTest.java` (adicionar testes)

- [ ] **Step 1: Adicionar testes que falham ao StationHandlerTest**

Adicionar os `@MockitoBean` novos e os métodos abaixo à classe existente:

```java
    @MockitoBean
    private UpdateStationUseCase updateStationUseCase;

    @MockitoBean
    private UpdateStationStatusUseCase updateStationStatusUseCase;

    @Test
    void putStation_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateStationUseCase.execute(eq(id), any(UpdateStationRequest.class)))
            .thenReturn(buildStation(id));

        mockMvc.perform(put("/api/stations/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new UpdateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void patchStationStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateStationStatusUseCase.execute(eq(id), any(UpdateStationStatusRequest.class)))
            .thenReturn(buildStation(id));

        mockMvc.perform(patch("/api/stations/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateStationStatusRequest(false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }
```

Imports novos no teste:
```java
import com.octane.station.usecase.station.UpdateStationRequest;
import com.octane.station.usecase.station.UpdateStationStatusRequest;
import com.octane.station.usecase.station.UpdateStationStatusUseCase;
import com.octane.station.usecase.station.UpdateStationUseCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=StationHandlerTest`
Expected: FAIL — 404/405 nos endpoints novos (handler ainda não os expõe). Atenção: os `@MockitoBean` novos só compilam depois da Task A1/A2 (já feitas).

- [ ] **Step 3: Implementar no StationHandler**

Adicionar dependências no construtor (campos `private final updateStationUseCase`, `updateStationStatusUseCase` + parâmetros) e os métodos:

```java
    @PutMapping("/{id}")
    public StationResponse update(@PathVariable UUID id, @RequestBody UpdateStationRequest request) {
        return StationResponse.from(updateStationUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/status")
    public StationResponse updateStatus(@PathVariable UUID id, @RequestBody UpdateStationStatusRequest request) {
        return StationResponse.from(updateStationStatusUseCase.execute(id, request));
    }
```

Imports novos no handler:
```java
import com.octane.station.usecase.station.UpdateStationRequest;
import com.octane.station.usecase.station.UpdateStationStatusRequest;
import com.octane.station.usecase.station.UpdateStationStatusUseCase;
import com.octane.station.usecase.station.UpdateStationUseCase;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest=StationHandlerTest`
Expected: todos PASS (6 antigos + 2 novos).

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/station/handler/StationHandler.java backend/src/test/java/com/octane/station/handler/StationHandlerTest.java
git commit -m "feat(station): add update and status endpoints for stations"
```

### Task A4: UpdatePumpUseCase e UpdatePumpStatusUseCase

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/pump/UpdatePumpRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/pump/UpdatePumpUseCase.java`
- Create: `backend/src/main/java/com/octane/station/usecase/pump/UpdatePumpStatusRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/pump/UpdatePumpStatusUseCase.java`
- Test: `backend/src/test/java/com/octane/station/usecase/pump/UpdatePumpUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/station/usecase/pump/UpdatePumpStatusUseCaseTest.java`

- [ ] **Step 1: Escrever os testes que falham**

`UpdatePumpUseCaseTest.java`:
```java
package com.octane.station.usecase.pump;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.PumpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePumpUseCaseTest {

    @Mock
    private PumpRepository pumpRepository;

    @InjectMocks
    private UpdatePumpUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

    @Test
    void execute_updatesNumber_whenNotDuplicated() {
        var id = UUID.randomUUID();
        var pump = new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(id)).thenReturn(Optional.of(pump));
        when(pumpRepository.existsByStationIdAndNumber(station.getId(), 2)).thenReturn(false);
        when(pumpRepository.save(any(Pump.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdatePumpRequest(2));

        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(PumpStatus.ACTIVE);
    }

    @Test
    void execute_keepsNumber_withoutDuplicateCheck_whenNumberUnchanged() {
        var id = UUID.randomUUID();
        var pump = new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(id)).thenReturn(Optional.of(pump));
        when(pumpRepository.save(any(Pump.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdatePumpRequest(1));

        assertThat(result.getNumber()).isEqualTo(1);
        verify(pumpRepository, never()).existsByStationIdAndNumber(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void execute_throwsBusinessException_whenNumberDuplicated() {
        var id = UUID.randomUUID();
        var pump = new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(id)).thenReturn(Optional.of(pump));
        when(pumpRepository.existsByStationIdAndNumber(station.getId(), 2)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(id, new UpdatePumpRequest(2)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Bomba");

        verify(pumpRepository, never()).save(any());
    }

    @Test
    void execute_throwsEntityNotFound_whenPumpMissing() {
        var id = UUID.randomUUID();
        when(pumpRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdatePumpRequest(2)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

`UpdatePumpStatusUseCaseTest.java`:
```java
package com.octane.station.usecase.pump;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.PumpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePumpStatusUseCaseTest {

    @Mock
    private PumpRepository pumpRepository;

    @InjectMocks
    private UpdatePumpStatusUseCase sut;

    @Test
    void execute_setsMaintenanceStatus() {
        var id = UUID.randomUUID();
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(id)).thenReturn(Optional.of(pump));
        when(pumpRepository.save(any(Pump.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdatePumpStatusRequest("MAINTENANCE"));

        assertThat(result.getStatus()).isEqualTo(PumpStatus.MAINTENANCE);
    }

    @Test
    void execute_throwsEntityNotFound_whenPumpMissing() {
        var id = UUID.randomUUID();
        when(pumpRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdatePumpStatusRequest("INACTIVE")))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest='UpdatePump*Test'`
Expected: erro de compilação.

- [ ] **Step 3: Implementar**

`UpdatePumpRequest.java`:
```java
package com.octane.station.usecase.pump;

public record UpdatePumpRequest(int number) {}
```

`UpdatePumpStatusRequest.java`:
```java
package com.octane.station.usecase.pump;

public record UpdatePumpStatusRequest(String status) {}
```

`UpdatePumpUseCase.java`:
```java
package com.octane.station.usecase.pump;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdatePumpUseCase {

    private final PumpRepository pumpRepository;

    public UpdatePumpUseCase(PumpRepository pumpRepository) {
        this.pumpRepository = pumpRepository;
    }

    @Transactional
    public Pump execute(UUID id, UpdatePumpRequest request) {
        var pump = pumpRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pump not found: " + id));

        if (request.number() != pump.getNumber()
                && pumpRepository.existsByStationIdAndNumber(pump.getStation().getId(), request.number())) {
            throw new BusinessException("Bomba número " + request.number() + " já existe neste posto");
        }

        var updated = new Pump(pump.getId(), request.number(), pump.getStatus(),
            pump.getStation(), pump.getCreatedAt(), LocalDateTime.now());
        return pumpRepository.save(updated);
    }
}
```

`UpdatePumpStatusUseCase.java`:
```java
package com.octane.station.usecase.pump;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdatePumpStatusUseCase {

    private final PumpRepository pumpRepository;

    public UpdatePumpStatusUseCase(PumpRepository pumpRepository) {
        this.pumpRepository = pumpRepository;
    }

    @Transactional
    public Pump execute(UUID id, UpdatePumpStatusRequest request) {
        var pump = pumpRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pump not found: " + id));

        PumpStatus status = PumpStatus.valueOf(request.status());

        var updated = new Pump(pump.getId(), pump.getNumber(), status,
            pump.getStation(), pump.getCreatedAt(), LocalDateTime.now());
        return pumpRepository.save(updated);
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest='UpdatePump*Test'`
Expected: 6 testes PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/station/usecase/pump/UpdatePump* backend/src/test/java/com/octane/station/usecase/pump/UpdatePump*
git commit -m "feat(station): add pump update and status use cases"
```

### Task A5: Endpoints PUT /api/pumps/{id} e PATCH /api/pumps/{id}/status

**Files:**
- Modify: `backend/src/main/java/com/octane/station/handler/PumpHandler.java`
- Test: `backend/src/test/java/com/octane/station/handler/PumpHandlerTest.java` (adicionar testes)

- [ ] **Step 1: Adicionar testes que falham**

No `PumpHandlerTest` existente, adicionar `@MockitoBean UpdatePumpUseCase updatePumpUseCase;` e `@MockitoBean UpdatePumpStatusUseCase updatePumpStatusUseCase;` e os testes (reaproveitar os builders existentes do teste; se não houver, usar os de `StationHandlerTest` como referência):

```java
    @Test
    void putPump_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(id, 2, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());
        when(updatePumpUseCase.execute(eq(id), any(UpdatePumpRequest.class))).thenReturn(pump);

        mockMvc.perform(put("/api/pumps/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdatePumpRequest(2))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(2));
    }

    @Test
    void patchPumpStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(id, 1, PumpStatus.MAINTENANCE, station, LocalDateTime.now(), LocalDateTime.now());
        when(updatePumpStatusUseCase.execute(eq(id), any(UpdatePumpStatusRequest.class))).thenReturn(pump);

        mockMvc.perform(patch("/api/pumps/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdatePumpStatusRequest("MAINTENANCE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }
```

Imports novos: `UpdatePumpRequest`, `UpdatePumpStatusRequest`, `UpdatePumpUseCase`, `UpdatePumpStatusUseCase`, `MockMvcRequestBuilders.put`, `MockMvcRequestBuilders.patch` (e `Station`/`Pump`/`PumpStatus`/`MediaType`/`LocalDateTime` se ainda não importados).

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=PumpHandlerTest`
Expected: FAIL — 404/405 nos endpoints novos.

- [ ] **Step 3: Implementar no PumpHandler**

Adicionar `private final UpdatePumpUseCase updatePumpUseCase;` e `private final UpdatePumpStatusUseCase updatePumpStatusUseCase;` ao construtor e os métodos:

```java
    @PutMapping("/{id}")
    public PumpResponse update(@PathVariable UUID id, @RequestBody UpdatePumpRequest request) {
        return PumpResponse.from(updatePumpUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/status")
    public PumpResponse updateStatus(@PathVariable UUID id, @RequestBody UpdatePumpStatusRequest request) {
        return PumpResponse.from(updatePumpStatusUseCase.execute(id, request));
    }
```

Imports: `UpdatePumpRequest`, `UpdatePumpStatusRequest`, `UpdatePumpUseCase`, `UpdatePumpStatusUseCase`, `PatchMapping`, `PutMapping`.

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest=PumpHandlerTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/station/handler/PumpHandler.java backend/src/test/java/com/octane/station/handler/PumpHandlerTest.java
git commit -m "feat(station): add pump update and status endpoints"
```

### Task A6: Update/status de bico + endpoints no NozzleHandler

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/nozzle/UpdateNozzleRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/nozzle/UpdateNozzleUseCase.java`
- Create: `backend/src/main/java/com/octane/station/usecase/nozzle/UpdateNozzleStatusRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/nozzle/UpdateNozzleStatusUseCase.java`
- Modify: `backend/src/main/java/com/octane/station/handler/NozzleHandler.java` (hoje é classe vazia)
- Test: `backend/src/test/java/com/octane/station/usecase/nozzle/UpdateNozzleUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/station/usecase/nozzle/UpdateNozzleStatusUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/station/handler/NozzleHandlerTest.java`

- [ ] **Step 1: Escrever os testes de use case que falham**

`UpdateNozzleUseCaseTest.java`:
```java
package com.octane.station.usecase.nozzle;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.NozzleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNozzleUseCaseTest {

    @Mock
    private NozzleRepository nozzleRepository;

    @Mock
    private FuelRepository fuelRepository;

    @InjectMocks
    private UpdateNozzleUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel gasolina = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Fuel etanol = new Fuel(UUID.randomUUID(), "Etanol", FuelUnit.LITER,
        true, LocalDateTime.now());

    @Test
    void execute_updatesNumberAndFuel() {
        var id = UUID.randomUUID();
        var nozzle = new Nozzle(id, 1, pump, gasolina, true, LocalDateTime.now(), LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(fuelRepository.findById(etanol.getId())).thenReturn(Optional.of(etanol));
        when(nozzleRepository.existsByPumpIdAndNumber(pump.getId(), 2)).thenReturn(false);
        when(nozzleRepository.save(any(Nozzle.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateNozzleRequest(2, etanol.getId()));

        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.getFuel().getId()).isEqualTo(etanol.getId());
    }

    @Test
    void execute_throwsBusinessException_whenNumberDuplicatedOnPump() {
        var id = UUID.randomUUID();
        var nozzle = new Nozzle(id, 1, pump, gasolina, true, LocalDateTime.now(), LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(fuelRepository.findById(gasolina.getId())).thenReturn(Optional.of(gasolina));
        when(nozzleRepository.existsByPumpIdAndNumber(pump.getId(), 2)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleRequest(2, gasolina.getId())))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Bico");

        verify(nozzleRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenFuelInactive() {
        var id = UUID.randomUUID();
        var nozzle = new Nozzle(id, 1, pump, gasolina, true, LocalDateTime.now(), LocalDateTime.now());
        var inactiveFuel = new Fuel(UUID.randomUUID(), "Diesel S500", FuelUnit.LITER, false, LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(fuelRepository.findById(inactiveFuel.getId())).thenReturn(Optional.of(inactiveFuel));

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleRequest(1, inactiveFuel.getId())))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    void execute_throwsEntityNotFound_whenNozzleMissing() {
        var id = UUID.randomUUID();
        when(nozzleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleRequest(1, UUID.randomUUID())))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

`UpdateNozzleStatusUseCaseTest.java`:
```java
package com.octane.station.usecase.nozzle;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.NozzleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNozzleStatusUseCaseTest {

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private UpdateNozzleStatusUseCase sut;

    @Test
    void execute_deactivatesNozzle() {
        var id = UUID.randomUUID();
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
            LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        var nozzle = new Nozzle(id, 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(nozzleRepository.save(any(Nozzle.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateNozzleStatusRequest(false));

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void execute_throwsEntityNotFound_whenNozzleMissing() {
        var id = UUID.randomUUID();
        when(nozzleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleStatusRequest(true)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest='UpdateNozzle*Test'`
Expected: erro de compilação.

- [ ] **Step 3: Implementar use cases**

`UpdateNozzleRequest.java`:
```java
package com.octane.station.usecase.nozzle;

import java.util.UUID;

public record UpdateNozzleRequest(int number, UUID fuelId) {}
```

`UpdateNozzleStatusRequest.java`:
```java
package com.octane.station.usecase.nozzle;

public record UpdateNozzleStatusRequest(boolean active) {}
```

`UpdateNozzleUseCase.java`:
```java
package com.octane.station.usecase.nozzle;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateNozzleUseCase {

    private final NozzleRepository nozzleRepository;
    private final FuelRepository fuelRepository;

    public UpdateNozzleUseCase(NozzleRepository nozzleRepository, FuelRepository fuelRepository) {
        this.nozzleRepository = nozzleRepository;
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public Nozzle execute(UUID id, UpdateNozzleRequest request) {
        var nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Nozzle not found: " + id));

        var fuel = fuelRepository.findById(request.fuelId())
            .orElseThrow(() -> new EntityNotFoundException("Fuel not found: " + request.fuelId()));

        if (!fuel.isActive()) {
            throw new BusinessException("Combustível inativo: " + fuel.getName());
        }

        if (request.number() != nozzle.getNumber()
                && nozzleRepository.existsByPumpIdAndNumber(nozzle.getPump().getId(), request.number())) {
            throw new BusinessException("Bico número " + request.number() + " já existe nesta bomba");
        }

        var updated = new Nozzle(nozzle.getId(), request.number(), nozzle.getPump(), fuel,
            nozzle.isActive(), nozzle.getCreatedAt(), LocalDateTime.now());
        return nozzleRepository.save(updated);
    }
}
```

`UpdateNozzleStatusUseCase.java`:
```java
package com.octane.station.usecase.nozzle;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateNozzleStatusUseCase {

    private final NozzleRepository nozzleRepository;

    public UpdateNozzleStatusUseCase(NozzleRepository nozzleRepository) {
        this.nozzleRepository = nozzleRepository;
    }

    @Transactional
    public Nozzle execute(UUID id, UpdateNozzleStatusRequest request) {
        var nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Nozzle not found: " + id));

        var updated = new Nozzle(nozzle.getId(), nozzle.getNumber(), nozzle.getPump(),
            nozzle.getFuel(), request.active(), nozzle.getCreatedAt(), LocalDateTime.now());
        return nozzleRepository.save(updated);
    }
}
```

- [ ] **Step 4: Rodar use cases e ver passar**

Run: `cd backend && ./mvnw test -Dtest='UpdateNozzle*Test'`
Expected: 6 testes PASS.

- [ ] **Step 5: Escrever NozzleHandlerTest (falha) e implementar o handler**

`NozzleHandlerTest.java` (novo arquivo):
```java
package com.octane.station.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.usecase.nozzle.UpdateNozzleRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusUseCase;
import com.octane.station.usecase.nozzle.UpdateNozzleUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NozzleHandler.class)
class NozzleHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UpdateNozzleUseCase updateNozzleUseCase;

    @MockitoBean
    private UpdateNozzleStatusUseCase updateNozzleStatusUseCase;

    private Nozzle buildNozzle(UUID id, boolean active) {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
            LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        return new Nozzle(id, 1, pump, fuel, active, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void putNozzle_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateNozzleUseCase.execute(eq(id), any(UpdateNozzleRequest.class)))
            .thenReturn(buildNozzle(id, true));

        mockMvc.perform(put("/api/nozzles/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateNozzleRequest(1, UUID.randomUUID()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void patchNozzleStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateNozzleStatusUseCase.execute(eq(id), any(UpdateNozzleStatusRequest.class)))
            .thenReturn(buildNozzle(id, false));

        mockMvc.perform(patch("/api/nozzles/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateNozzleStatusRequest(false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }
}
```

`NozzleHandler.java` (substituir a classe vazia):
```java
package com.octane.station.handler;

import com.octane.station.usecase.nozzle.UpdateNozzleRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusUseCase;
import com.octane.station.usecase.nozzle.UpdateNozzleUseCase;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/nozzles")
public class NozzleHandler {

    private final UpdateNozzleUseCase updateNozzleUseCase;
    private final UpdateNozzleStatusUseCase updateNozzleStatusUseCase;

    public NozzleHandler(UpdateNozzleUseCase updateNozzleUseCase,
                         UpdateNozzleStatusUseCase updateNozzleStatusUseCase) {
        this.updateNozzleUseCase = updateNozzleUseCase;
        this.updateNozzleStatusUseCase = updateNozzleStatusUseCase;
    }

    @PutMapping("/{id}")
    public NozzleResponse update(@PathVariable UUID id, @RequestBody UpdateNozzleRequest request) {
        return NozzleResponse.from(updateNozzleUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/status")
    public NozzleResponse updateStatus(@PathVariable UUID id, @RequestBody UpdateNozzleStatusRequest request) {
        return NozzleResponse.from(updateNozzleStatusUseCase.execute(id, request));
    }
}
```

- [ ] **Step 6: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest='*Nozzle*Test'`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/octane/station/usecase/nozzle/UpdateNozzle* backend/src/main/java/com/octane/station/handler/NozzleHandler.java backend/src/test/java/com/octane/station/usecase/nozzle/UpdateNozzle* backend/src/test/java/com/octane/station/handler/NozzleHandlerTest.java
git commit -m "feat(station): add nozzle update and status use cases and endpoints"
```

### Task A7: Inativação de combustível (save no FuelRepository + use case + endpoint)

**Files:**
- Modify: `backend/src/main/java/com/octane/station/domain/repository/FuelRepository.java` (adicionar `save`)
- Modify: `backend/src/main/java/com/octane/station/repository/FuelRepositoryImpl.java` (implementar `save`)
- Create: `backend/src/main/java/com/octane/station/usecase/fuel/UpdateFuelStatusRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/fuel/UpdateFuelStatusUseCase.java`
- Modify: `backend/src/main/java/com/octane/station/handler/FuelHandler.java`
- Test: `backend/src/test/java/com/octane/station/usecase/fuel/UpdateFuelStatusUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/station/handler/FuelHandlerTest.java` (adicionar teste)

- [ ] **Step 1: Escrever o teste que falha**

`UpdateFuelStatusUseCaseTest.java`:
```java
package com.octane.station.usecase.fuel;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.repository.FuelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFuelStatusUseCaseTest {

    @Mock
    private FuelRepository fuelRepository;

    @InjectMocks
    private UpdateFuelStatusUseCase sut;

    @Test
    void execute_deactivatesFuel() {
        var id = UUID.randomUUID();
        var fuel = new Fuel(id, "Diesel S500", FuelUnit.LITER, true, LocalDateTime.now());

        when(fuelRepository.findById(id)).thenReturn(Optional.of(fuel));
        when(fuelRepository.save(any(Fuel.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateFuelStatusRequest(false));

        assertThat(result.isActive()).isFalse();
        assertThat(result.getName()).isEqualTo("Diesel S500");
    }

    @Test
    void execute_throwsEntityNotFound_whenFuelMissing() {
        var id = UUID.randomUUID();
        when(fuelRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateFuelStatusRequest(true)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=UpdateFuelStatusUseCaseTest`
Expected: erro de compilação (use case não existe; `FuelRepository.save` não existe).

- [ ] **Step 3: Implementar**

Em `FuelRepository.java`, adicionar à interface:
```java
    Fuel save(Fuel fuel);
```

Em `FuelRepositoryImpl.java`, adicionar:
```java
    @Override
    public Fuel save(Fuel fuel) {
        return jpaRepository.save(fuel);
    }
```

`UpdateFuelStatusRequest.java`:
```java
package com.octane.station.usecase.fuel;

public record UpdateFuelStatusRequest(boolean active) {}
```

`UpdateFuelStatusUseCase.java`:
```java
package com.octane.station.usecase.fuel;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFuelStatusUseCase {

    private final FuelRepository fuelRepository;

    public UpdateFuelStatusUseCase(FuelRepository fuelRepository) {
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public Fuel execute(UUID id, UpdateFuelStatusRequest request) {
        var fuel = fuelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Fuel not found: " + id));

        var updated = new Fuel(fuel.getId(), fuel.getName(), fuel.getUnit(),
            request.active(), fuel.getCreatedAt());
        return fuelRepository.save(updated);
    }
}
```

No `FuelHandler.java`, injetar `UpdateFuelStatusUseCase` no construtor e adicionar:
```java
    @PatchMapping("/{id}/status")
    public FuelResponse updateStatus(@PathVariable UUID id, @RequestBody UpdateFuelStatusRequest request) {
        return FuelResponse.from(updateFuelStatusUseCase.execute(id, request));
    }
```
Imports: `UpdateFuelStatusRequest`, `UpdateFuelStatusUseCase`, `PatchMapping`, `PathVariable`, `RequestBody`, `java.util.UUID`.

No `FuelHandlerTest`, adicionar `@MockitoBean UpdateFuelStatusUseCase updateFuelStatusUseCase;` e:
```java
    @Test
    void patchFuelStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        var fuel = new Fuel(id, "Diesel S500", FuelUnit.LITER, false, LocalDateTime.now());
        when(updateFuelStatusUseCase.execute(eq(id), any(UpdateFuelStatusRequest.class))).thenReturn(fuel);

        mockMvc.perform(patch("/api/fuels/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateFuelStatusRequest(false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }
```
(Se o teste não tiver `ObjectMapper`, declarar `private final ObjectMapper objectMapper = new ObjectMapper();`.)

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest='UpdateFuelStatusUseCaseTest,FuelHandlerTest'`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/station/domain/repository/FuelRepository.java backend/src/main/java/com/octane/station/repository/FuelRepositoryImpl.java backend/src/main/java/com/octane/station/usecase/fuel/ backend/src/main/java/com/octane/station/handler/FuelHandler.java backend/src/test/java/com/octane/station/usecase/fuel/ backend/src/test/java/com/octane/station/handler/FuelHandlerTest.java
git commit -m "feat(station): add fuel status endpoint"
```

### Task A8: OpenShiftUseCase valida posto ativo

**Files:**
- Modify: `backend/src/main/java/com/octane/fueling/usecase/shift/OpenShiftUseCase.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/shift/OpenShiftUseCaseTest.java` (adicionar teste)

- [ ] **Step 1: Adicionar teste que falha**

No `OpenShiftUseCaseTest` existente, adicionar:
```java
    @Test
    void execute_throwsBusinessException_whenStationInactive() {
        var stationId = UUID.randomUUID();
        var inactiveStation = new Station(stationId, "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", false, LocalDateTime.now(), LocalDateTime.now());
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(inactiveStation));

        assertThatThrownBy(() -> sut.execute(new OpenShiftRequest(stationId, "João", null)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");

        verify(shiftRepository, never()).save(any());
    }
```
(Conferir nomes dos mocks/imports existentes no arquivo e reusar.)

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=OpenShiftUseCaseTest`
Expected: FAIL — exceção não lançada.

- [ ] **Step 3: Implementar**

No `OpenShiftUseCase.execute`, logo após obter `station`:
```java
        if (!station.isActive()) {
            throw new BusinessException("Posto inativo: não é possível abrir turno");
        }
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest=OpenShiftUseCaseTest`
Expected: PASS.

- [ ] **Step 5: Rodar a suite completa da Fase A e commitar**

Run: `cd backend && ./mvnw test`
Expected: BUILD SUCCESS, zero falhas.

```bash
git add backend/src/main/java/com/octane/fueling/usecase/shift/OpenShiftUseCase.java backend/src/test/java/com/octane/fueling/usecase/shift/OpenShiftUseCaseTest.java
git commit -m "feat(fueling): reject opening shift on inactive station"
```

---

## Fase B — Módulo `pricing`

### Task B1: Migration V8 + entidade FuelPrice + repositórios

**Files:**
- Create: `backend/src/main/resources/db/migration/V8__create_fuel_prices.sql`
- Create: `backend/src/main/java/com/octane/pricing/domain/FuelPrice.java`
- Create: `backend/src/main/java/com/octane/pricing/domain/repository/FuelPriceRepository.java`
- Create: `backend/src/main/java/com/octane/pricing/repository/FuelPriceJpaRepository.java`
- Create: `backend/src/main/java/com/octane/pricing/repository/FuelPriceRepositoryImpl.java`

- [ ] **Step 1: Criar a migration**

`V8__create_fuel_prices.sql`:
```sql
CREATE TABLE fuel_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL REFERENCES stations(id),
    fuel_id UUID NOT NULL REFERENCES fuels(id),
    price NUMERIC(10,4) NOT NULL,
    effective_from TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fuel_prices_lookup
    ON fuel_prices (station_id, fuel_id, effective_from DESC);
```

- [ ] **Step 2: Criar entidade e repositórios**

`FuelPrice.java`:
```java
package com.octane.pricing.domain;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fuel_prices")
public class FuelPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne
    @JoinColumn(name = "fuel_id", nullable = false)
    private Fuel fuel;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal price;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FuelPrice() {}

    public FuelPrice(UUID id, Station station, Fuel fuel, BigDecimal price,
                     LocalDateTime effectiveFrom, LocalDateTime createdAt) {
        this.id = id;
        this.station = station;
        this.fuel = fuel;
        this.price = price;
        this.effectiveFrom = effectiveFrom;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Station getStation() { return station; }
    public Fuel getFuel() { return fuel; }
    public BigDecimal getPrice() { return price; }
    public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

`FuelPriceRepository.java`:
```java
package com.octane.pricing.domain.repository;

import com.octane.pricing.domain.FuelPrice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelPriceRepository {
    FuelPrice save(FuelPrice fuelPrice);
    Optional<FuelPrice> findCurrent(UUID stationId, UUID fuelId);
    List<FuelPrice> findCurrentByStation(UUID stationId);
    List<FuelPrice> findHistory(UUID stationId, UUID fuelId);
}
```

`FuelPriceJpaRepository.java`:
```java
package com.octane.pricing.repository;

import com.octane.pricing.domain.FuelPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FuelPriceJpaRepository extends JpaRepository<FuelPrice, UUID> {
    Optional<FuelPrice> findFirstByStation_IdAndFuel_IdOrderByEffectiveFromDesc(UUID stationId, UUID fuelId);
    List<FuelPrice> findByStation_IdOrderByEffectiveFromDesc(UUID stationId);
    List<FuelPrice> findByStation_IdAndFuel_IdOrderByEffectiveFromDesc(UUID stationId, UUID fuelId);
}
```

`FuelPriceRepositoryImpl.java`:
```java
package com.octane.pricing.repository;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FuelPriceRepositoryImpl implements FuelPriceRepository {

    private final FuelPriceJpaRepository jpaRepository;

    public FuelPriceRepositoryImpl(FuelPriceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FuelPrice save(FuelPrice fuelPrice) {
        return jpaRepository.save(fuelPrice);
    }

    @Override
    public Optional<FuelPrice> findCurrent(UUID stationId, UUID fuelId) {
        return jpaRepository.findFirstByStation_IdAndFuel_IdOrderByEffectiveFromDesc(stationId, fuelId);
    }

    @Override
    public List<FuelPrice> findCurrentByStation(UUID stationId) {
        var seenFuels = new HashSet<UUID>();
        return jpaRepository.findByStation_IdOrderByEffectiveFromDesc(stationId).stream()
            .filter(price -> seenFuels.add(price.getFuel().getId()))
            .toList();
    }

    @Override
    public List<FuelPrice> findHistory(UUID stationId, UUID fuelId) {
        return jpaRepository.findByStation_IdAndFuel_IdOrderByEffectiveFromDesc(stationId, fuelId);
    }
}
```

- [ ] **Step 3: Compilar e commitar**

Run: `cd backend && ./mvnw test-compile`
Expected: BUILD SUCCESS.

```bash
git add backend/src/main/resources/db/migration/V8__create_fuel_prices.sql backend/src/main/java/com/octane/pricing/
git commit -m "feat(pricing): add FuelPrice entity, migration and repositories"
```

### Task B2: SetFuelPriceUseCase

**Files:**
- Create: `backend/src/main/java/com/octane/pricing/usecase/SetFuelPriceRequest.java`
- Create: `backend/src/main/java/com/octane/pricing/usecase/SetFuelPriceUseCase.java`
- Test: `backend/src/test/java/com/octane/pricing/usecase/SetFuelPriceUseCaseTest.java`

- [ ] **Step 1: Escrever o teste que falha**

```java
package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetFuelPriceUseCaseTest {

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private FuelRepository fuelRepository;

    @InjectMocks
    private SetFuelPriceUseCase sut;

    private Station buildStation(boolean active) {
        return new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", active, LocalDateTime.now(), LocalDateTime.now());
    }

    private Fuel buildFuel(boolean active) {
        return new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, active, LocalDateTime.now());
    }

    @Test
    void execute_savesPrice_whenStationAndFuelActive() {
        var station = buildStation(true);
        var fuel = buildFuel(true);

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuel.getId())).thenReturn(Optional.of(fuel));
        when(fuelPriceRepository.save(any(FuelPrice.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(station.getId(), new SetFuelPriceRequest(fuel.getId(), new BigDecimal("5.8990")));

        assertThat(result.getPrice()).isEqualByComparingTo("5.8990");
        assertThat(result.getStation().getId()).isEqualTo(station.getId());
        assertThat(result.getFuel().getId()).isEqualTo(fuel.getId());
        assertThat(result.getEffectiveFrom()).isNotNull();
    }

    @Test
    void execute_throwsBusinessException_whenPriceNotPositive() {
        var station = buildStation(true);
        var fuel = buildFuel(true);
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuel.getId())).thenReturn(Optional.of(fuel));

        assertThatThrownBy(() -> sut.execute(station.getId(),
            new SetFuelPriceRequest(fuel.getId(), BigDecimal.ZERO)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Preço");

        verify(fuelPriceRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenStationInactive() {
        var station = buildStation(false);
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));

        assertThatThrownBy(() -> sut.execute(station.getId(),
            new SetFuelPriceRequest(UUID.randomUUID(), new BigDecimal("5.89"))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    void execute_throwsBusinessException_whenFuelInactive() {
        var station = buildStation(true);
        var fuel = buildFuel(false);
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuel.getId())).thenReturn(Optional.of(fuel));

        assertThatThrownBy(() -> sut.execute(station.getId(),
            new SetFuelPriceRequest(fuel.getId(), new BigDecimal("5.89"))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId,
            new SetFuelPriceRequest(UUID.randomUUID(), new BigDecimal("5.89"))))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=SetFuelPriceUseCaseTest`
Expected: erro de compilação.

- [ ] **Step 3: Implementar**

`SetFuelPriceRequest.java`:
```java
package com.octane.pricing.usecase;

import java.math.BigDecimal;
import java.util.UUID;

public record SetFuelPriceRequest(UUID fuelId, BigDecimal price) {}
```

`SetFuelPriceUseCase.java`:
```java
package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SetFuelPriceUseCase {

    private final FuelPriceRepository fuelPriceRepository;
    private final StationRepository stationRepository;
    private final FuelRepository fuelRepository;

    public SetFuelPriceUseCase(FuelPriceRepository fuelPriceRepository,
                               StationRepository stationRepository,
                               FuelRepository fuelRepository) {
        this.fuelPriceRepository = fuelPriceRepository;
        this.stationRepository = stationRepository;
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public FuelPrice execute(UUID stationId, SetFuelPriceRequest request) {
        var station = stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));

        if (!station.isActive()) {
            throw new BusinessException("Posto inativo: não é possível cadastrar preço");
        }

        var fuel = fuelRepository.findById(request.fuelId())
            .orElseThrow(() -> new EntityNotFoundException("Fuel not found: " + request.fuelId()));

        if (!fuel.isActive()) {
            throw new BusinessException("Combustível inativo: " + fuel.getName());
        }

        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Preço deve ser maior que zero");
        }

        var now = LocalDateTime.now();
        var fuelPrice = new FuelPrice(null, station, fuel, request.price(), now, now);
        return fuelPriceRepository.save(fuelPrice);
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest=SetFuelPriceUseCaseTest`
Expected: 5 testes PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/pricing/usecase/ backend/src/test/java/com/octane/pricing/
git commit -m "feat(pricing): add SetFuelPriceUseCase"
```

### Task B3: GetCurrentPricesUseCase e ListPriceHistoryUseCase

**Files:**
- Create: `backend/src/main/java/com/octane/pricing/usecase/GetCurrentPricesUseCase.java`
- Create: `backend/src/main/java/com/octane/pricing/usecase/ListPriceHistoryUseCase.java`
- Test: `backend/src/test/java/com/octane/pricing/usecase/GetCurrentPricesUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/pricing/usecase/ListPriceHistoryUseCaseTest.java`

- [ ] **Step 1: Escrever os testes que falham**

`GetCurrentPricesUseCaseTest.java`:
```java
package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentPricesUseCaseTest {

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private GetCurrentPricesUseCase sut;

    @Test
    void execute_returnsCurrentPrices() {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Etanol", FuelUnit.LITER, true, LocalDateTime.now());
        var price = new FuelPrice(UUID.randomUUID(), station, fuel, new BigDecimal("3.99"),
            LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelPriceRepository.findCurrentByStation(station.getId())).thenReturn(List.of(price));

        var result = sut.execute(station.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("3.99");
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

`ListPriceHistoryUseCaseTest.java`:
```java
package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPriceHistoryUseCaseTest {

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private ListPriceHistoryUseCase sut;

    @Test
    void execute_returnsHistoryNewestFirst() {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Etanol", FuelUnit.LITER, true, LocalDateTime.now());
        var newer = new FuelPrice(UUID.randomUUID(), station, fuel, new BigDecimal("4.09"),
            LocalDateTime.now(), LocalDateTime.now());
        var older = new FuelPrice(UUID.randomUUID(), station, fuel, new BigDecimal("3.99"),
            LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1));

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelPriceRepository.findHistory(station.getId(), fuel.getId()))
            .thenReturn(List.of(newer, older));

        var result = sut.execute(station.getId(), fuel.getId());

        assertThat(result).containsExactly(newer, older);
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId, UUID.randomUUID()))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest='GetCurrentPricesUseCaseTest,ListPriceHistoryUseCaseTest'`
Expected: erro de compilação.

- [ ] **Step 3: Implementar**

`GetCurrentPricesUseCase.java`:
```java
package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetCurrentPricesUseCase {

    private final FuelPriceRepository fuelPriceRepository;
    private final StationRepository stationRepository;

    public GetCurrentPricesUseCase(FuelPriceRepository fuelPriceRepository,
                                   StationRepository stationRepository) {
        this.fuelPriceRepository = fuelPriceRepository;
        this.stationRepository = stationRepository;
    }

    public List<FuelPrice> execute(UUID stationId) {
        stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));
        return fuelPriceRepository.findCurrentByStation(stationId);
    }
}
```

`ListPriceHistoryUseCase.java`:
```java
package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListPriceHistoryUseCase {

    private final FuelPriceRepository fuelPriceRepository;
    private final StationRepository stationRepository;

    public ListPriceHistoryUseCase(FuelPriceRepository fuelPriceRepository,
                                   StationRepository stationRepository) {
        this.fuelPriceRepository = fuelPriceRepository;
        this.stationRepository = stationRepository;
    }

    public List<FuelPrice> execute(UUID stationId, UUID fuelId) {
        stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));
        return fuelPriceRepository.findHistory(stationId, fuelId);
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest='GetCurrentPricesUseCaseTest,ListPriceHistoryUseCaseTest'`
Expected: 4 testes PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/pricing/usecase/ backend/src/test/java/com/octane/pricing/usecase/
git commit -m "feat(pricing): add current prices and price history use cases"
```

### Task B4: FuelPriceHandler

**Files:**
- Create: `backend/src/main/java/com/octane/pricing/handler/FuelPriceResponse.java`
- Create: `backend/src/main/java/com/octane/pricing/handler/FuelPriceHandler.java`
- Test: `backend/src/test/java/com/octane/pricing/handler/FuelPriceHandlerTest.java`

- [ ] **Step 1: Escrever o teste que falha**

```java
package com.octane.pricing.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.usecase.GetCurrentPricesUseCase;
import com.octane.pricing.usecase.ListPriceHistoryUseCase;
import com.octane.pricing.usecase.SetFuelPriceRequest;
import com.octane.pricing.usecase.SetFuelPriceUseCase;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FuelPriceHandler.class)
class FuelPriceHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SetFuelPriceUseCase setFuelPriceUseCase;

    @MockitoBean
    private GetCurrentPricesUseCase getCurrentPricesUseCase;

    @MockitoBean
    private ListPriceHistoryUseCase listPriceHistoryUseCase;

    private FuelPrice buildPrice(UUID stationId, UUID fuelId) {
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(fuelId, "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        return new FuelPrice(UUID.randomUUID(), station, fuel, new BigDecimal("5.8990"),
            LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void postPrice_returns201WithBody() throws Exception {
        var stationId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(setFuelPriceUseCase.execute(eq(stationId), any(SetFuelPriceRequest.class)))
            .thenReturn(buildPrice(stationId, fuelId));

        mockMvc.perform(post("/api/stations/" + stationId + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SetFuelPriceRequest(fuelId, new BigDecimal("5.8990")))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.price").value(5.8990))
            .andExpect(jsonPath("$.fuelName").value("Gasolina Comum"));
    }

    @Test
    void getPrices_returns200WithList() throws Exception {
        var stationId = UUID.randomUUID();
        when(getCurrentPricesUseCase.execute(stationId))
            .thenReturn(List.of(buildPrice(stationId, UUID.randomUUID())));

        mockMvc.perform(get("/api/stations/" + stationId + "/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].fuelName").value("Gasolina Comum"));
    }

    @Test
    void getPriceHistory_returns200WithList() throws Exception {
        var stationId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(listPriceHistoryUseCase.execute(stationId, fuelId))
            .thenReturn(List.of(buildPrice(stationId, fuelId)));

        mockMvc.perform(get("/api/stations/" + stationId + "/prices/history?fuelId=" + fuelId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].price").value(5.8990));
    }
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=FuelPriceHandlerTest`
Expected: erro de compilação.

- [ ] **Step 3: Implementar**

`FuelPriceResponse.java`:
```java
package com.octane.pricing.handler;

import com.octane.pricing.domain.FuelPrice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FuelPriceResponse(
    UUID id,
    UUID fuelId,
    String fuelName,
    BigDecimal price,
    LocalDateTime effectiveFrom
) {
    public static FuelPriceResponse from(FuelPrice fuelPrice) {
        return new FuelPriceResponse(
            fuelPrice.getId(),
            fuelPrice.getFuel().getId(),
            fuelPrice.getFuel().getName(),
            fuelPrice.getPrice(),
            fuelPrice.getEffectiveFrom()
        );
    }
}
```

`FuelPriceHandler.java`:
```java
package com.octane.pricing.handler;

import com.octane.pricing.usecase.GetCurrentPricesUseCase;
import com.octane.pricing.usecase.ListPriceHistoryUseCase;
import com.octane.pricing.usecase.SetFuelPriceRequest;
import com.octane.pricing.usecase.SetFuelPriceUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stations/{stationId}/prices")
public class FuelPriceHandler {

    private final SetFuelPriceUseCase setFuelPriceUseCase;
    private final GetCurrentPricesUseCase getCurrentPricesUseCase;
    private final ListPriceHistoryUseCase listPriceHistoryUseCase;

    public FuelPriceHandler(
        SetFuelPriceUseCase setFuelPriceUseCase,
        GetCurrentPricesUseCase getCurrentPricesUseCase,
        ListPriceHistoryUseCase listPriceHistoryUseCase
    ) {
        this.setFuelPriceUseCase = setFuelPriceUseCase;
        this.getCurrentPricesUseCase = getCurrentPricesUseCase;
        this.listPriceHistoryUseCase = listPriceHistoryUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuelPriceResponse setPrice(@PathVariable UUID stationId, @RequestBody SetFuelPriceRequest request) {
        return FuelPriceResponse.from(setFuelPriceUseCase.execute(stationId, request));
    }

    @GetMapping
    public List<FuelPriceResponse> currentPrices(@PathVariable UUID stationId) {
        return getCurrentPricesUseCase.execute(stationId).stream()
            .map(FuelPriceResponse::from)
            .toList();
    }

    @GetMapping("/history")
    public List<FuelPriceResponse> history(@PathVariable UUID stationId, @RequestParam UUID fuelId) {
        return listPriceHistoryUseCase.execute(stationId, fuelId).stream()
            .map(FuelPriceResponse::from)
            .toList();
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test -Dtest=FuelPriceHandlerTest`
Expected: 3 testes PASS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/pricing/handler/ backend/src/test/java/com/octane/pricing/handler/
git commit -m "feat(pricing): add fuel price endpoints"
```

---

## Fase C — Abastecimento integrado ao preço + cancelamento

### Task C1: FuelingStatus + migration V9 + colunas na entidade Fueling

**Files:**
- Create: `backend/src/main/resources/db/migration/V9__add_status_to_fuelings.sql`
- Create: `backend/src/main/java/com/octane/fueling/domain/FuelingStatus.java`
- Modify: `backend/src/main/java/com/octane/fueling/domain/Fueling.java`
- Modify: todos os pontos que chamam o construtor de `Fueling` (compilador aponta: `RegisterFuelingUseCase`, testes)

- [ ] **Step 1: Criar migration e enum**

`V9__add_status_to_fuelings.sql`:
```sql
ALTER TABLE fuelings
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN canceled_at TIMESTAMP;
```

`FuelingStatus.java`:
```java
package com.octane.fueling.domain;

public enum FuelingStatus {
    ACTIVE, CANCELED
}
```

- [ ] **Step 2: Alterar a entidade Fueling**

Adicionar os campos (após `paymentMethod`):
```java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuelingStatus status;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
```

Novo construtor completo (substitui o atual — `status` e `canceledAt` entram após `paymentMethod`):
```java
    public Fueling(UUID id, Shift shift, Nozzle nozzle, BigDecimal liters, BigDecimal unitPrice,
                   BigDecimal totalAmount, PaymentMethod paymentMethod, FuelingStatus status,
                   LocalDateTime canceledAt, String vehiclePlate, String notes,
                   LocalDateTime fueledAt, LocalDateTime createdAt) {
        this.id = id;
        this.shift = shift;
        this.nozzle = nozzle;
        this.liters = liters;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.canceledAt = canceledAt;
        this.vehiclePlate = vehiclePlate;
        this.notes = notes;
        this.fueledAt = fueledAt;
        this.createdAt = createdAt;
    }
```

Getters novos:
```java
    public FuelingStatus getStatus() { return status; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
```

- [ ] **Step 3: Corrigir os chamadores do construtor**

Rodar `cd backend && ./mvnw test-compile` e corrigir cada erro:
- Em `RegisterFuelingUseCase`: passar `FuelingStatus.ACTIVE, null` nas posições novas (import `com.octane.fueling.domain.FuelingStatus`).
- Em testes que constroem `Fueling` (`RegisterFuelingUseCaseTest`, `ListFuelingsByShiftUseCaseTest`, `FuelingHandlerTest`): mesmo ajuste.

- [ ] **Step 4: Rodar a suite e ver passar**

Run: `cd backend && ./mvnw test`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/db/migration/V9__add_status_to_fuelings.sql backend/src/main/java/com/octane/fueling/ backend/src/test/java/com/octane/fueling/
git commit -m "feat(fueling): add status and canceledAt to Fueling"
```

### Task C2: RegisterFuelingUseCase com preço da tabela e cadeia ativa

**Files:**
- Modify: `backend/src/main/java/com/octane/fueling/usecase/fueling/RegisterFuelingRequest.java`
- Modify: `backend/src/main/java/com/octane/fueling/usecase/fueling/RegisterFuelingUseCase.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/fueling/RegisterFuelingUseCaseTest.java` (reescrever os cenários de preço)
- Modify: `backend/src/test/java/com/octane/fueling/handler/FuelingHandlerTest.java` (ajustar request)

- [ ] **Step 1: Novo contrato do request**

`RegisterFuelingRequest.java` (substituir — sai `unitPrice`):
```java
package com.octane.fueling.usecase.fueling;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterFuelingRequest(
    UUID nozzleId,
    BigDecimal liters,
    BigDecimal totalAmount,
    String paymentMethod,
    String vehiclePlate,
    String notes
) {}
```

- [ ] **Step 2: Reescrever o teste do use case (falha primeiro)**

Substituir `RegisterFuelingUseCaseTest.java` por:
```java
package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.NozzleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterFuelingUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private FuelingRepository fuelingRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @InjectMocks
    private RegisterFuelingUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Nozzle nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true,
        LocalDateTime.now(), LocalDateTime.now());
    private final Shift openShift = new Shift(UUID.randomUUID(), station, "João",
        ShiftStatus.OPEN, LocalDateTime.now(), null, null, LocalDateTime.now());
    private final FuelPrice currentPrice = new FuelPrice(UUID.randomUUID(), station, fuel,
        new BigDecimal("5.00"), LocalDateTime.now(), LocalDateTime.now());

    private void stubHappyPath() {
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(nozzle.getId())).thenReturn(Optional.of(nozzle));
        when(fuelPriceRepository.findCurrent(station.getId(), fuel.getId()))
            .thenReturn(Optional.of(currentPrice));
    }

    @Test
    void execute_computesTotalFromLiters_usingCurrentPrice() {
        stubHappyPath();
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        var result = sut.execute(openShift.getId(), request);

        assertThat(result.getLiters()).isEqualByComparingTo("10.000");
        assertThat(result.getUnitPrice()).isEqualByComparingTo("5.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void execute_computesLitersFromTotal_usingCurrentPrice() {
        stubHappyPath();
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFuelingRequest(nozzle.getId(), null,
            new BigDecimal("50.00"), "CASH", null, null);

        var result = sut.execute(openShift.getId(), request);

        assertThat(result.getLiters()).isEqualByComparingTo("10.000");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void execute_acceptsBothValues_whenConsistentWithinTolerance() {
        stubHappyPath();
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            new BigDecimal("50.01"), "PIX", null, null);

        var result = sut.execute(openShift.getId(), request);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("50.01");
    }

    @Test
    void execute_throwsBusinessException_whenBothValuesInconsistent() {
        stubHappyPath();

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            new BigDecimal("55.00"), "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("não confere");

        verify(fuelingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenNeitherLitersNorTotalGiven() {
        stubHappyPath();

        var request = new RegisterFuelingRequest(nozzle.getId(), null, null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("litros ou valor total");
    }

    @Test
    void execute_throwsBusinessException_whenNoPriceRegistered() {
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(nozzle.getId())).thenReturn(Optional.of(nozzle));
        when(fuelPriceRepository.findCurrent(station.getId(), fuel.getId()))
            .thenReturn(Optional.empty());

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("preço");
    }

    @Test
    void execute_throwsBusinessException_whenNozzleInactive() {
        var inactiveNozzle = new Nozzle(UUID.randomUUID(), 2, pump, fuel, false,
            LocalDateTime.now(), LocalDateTime.now());
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(inactiveNozzle.getId())).thenReturn(Optional.of(inactiveNozzle));

        var request = new RegisterFuelingRequest(inactiveNozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativ");
    }

    @Test
    void execute_throwsBusinessException_whenPumpNotActive() {
        var maintenancePump = new Pump(UUID.randomUUID(), 2, PumpStatus.MAINTENANCE, station,
            LocalDateTime.now(), LocalDateTime.now());
        var nozzleOnMaintenancePump = new Nozzle(UUID.randomUUID(), 1, maintenancePump, fuel, true,
            LocalDateTime.now(), LocalDateTime.now());
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(nozzleOnMaintenancePump.getId()))
            .thenReturn(Optional.of(nozzleOnMaintenancePump));

        var request = new RegisterFuelingRequest(nozzleOnMaintenancePump.getId(),
            new BigDecimal("10.000"), null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Bomba");
    }

    @Test
    void execute_throwsBusinessException_whenShiftNotOpen() {
        var closedShift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now());
        when(shiftRepository.findById(closedShift.getId())).thenReturn(Optional.of(closedShift));

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(closedShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("aberto");
    }

    @Test
    void execute_throwsBusinessException_whenNozzleBelongsToAnotherStation() {
        var otherStation = new Station(UUID.randomUUID(), "Posto Y", "99.999.999/0001-99",
            "Rua B, 2", "Campinas", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var otherPump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, otherStation,
            LocalDateTime.now(), LocalDateTime.now());
        var foreignNozzle = new Nozzle(UUID.randomUUID(), 1, otherPump, fuel, true,
            LocalDateTime.now(), LocalDateTime.now());
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(foreignNozzle.getId())).thenReturn(Optional.of(foreignNozzle));

        var request = new RegisterFuelingRequest(foreignNozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("posto");
    }

    @Test
    void execute_throwsEntityNotFound_whenShiftMissing() {
        var shiftId = UUID.randomUUID();
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(shiftId,
            new RegisterFuelingRequest(nozzle.getId(), BigDecimal.ONE, null, "PIX", null, null)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 3: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=RegisterFuelingUseCaseTest`
Expected: erros de compilação (request mudou, mock de `FuelPriceRepository` não injetado).

- [ ] **Step 4: Implementar o use case**

Substituir o corpo de `RegisterFuelingUseCase.java` por:
```java
package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterFuelingUseCase {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    private final ShiftRepository shiftRepository;
    private final FuelingRepository fuelingRepository;
    private final NozzleRepository nozzleRepository;
    private final FuelPriceRepository fuelPriceRepository;

    public RegisterFuelingUseCase(ShiftRepository shiftRepository,
                                  FuelingRepository fuelingRepository,
                                  NozzleRepository nozzleRepository,
                                  FuelPriceRepository fuelPriceRepository) {
        this.shiftRepository = shiftRepository;
        this.fuelingRepository = fuelingRepository;
        this.nozzleRepository = nozzleRepository;
        this.fuelPriceRepository = fuelPriceRepository;
    }

    @Transactional
    public Fueling execute(UUID shiftId, RegisterFuelingRequest request) {
        var shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + shiftId));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessException("Turno não está aberto");
        }

        var nozzleId = request.nozzleId();
        var nozzle = nozzleRepository.findById(nozzleId)
                .orElseThrow(() -> new EntityNotFoundException("Nozzle not found: " + nozzleId));

        if (!nozzle.getPump().getStation().getId().equals(shift.getStation().getId())) {
            throw new BusinessException("Bico não pertence ao posto deste turno");
        }

        if (!shift.getStation().isActive()) {
            throw new BusinessException("Posto inativo");
        }
        if (nozzle.getPump().getStatus() != PumpStatus.ACTIVE) {
            throw new BusinessException("Bomba não está ativa");
        }
        if (!nozzle.isActive()) {
            throw new BusinessException("Bico inativo");
        }

        var stationId = shift.getStation().getId();
        var fuelId = nozzle.getFuel().getId();
        var price = fuelPriceRepository.findCurrent(stationId, fuelId)
                .orElseThrow(() -> new BusinessException(
                    "Sem preço vigente para o combustível " + nozzle.getFuel().getName()))
                .getPrice();

        BigDecimal liters = request.liters();
        BigDecimal totalAmount = request.totalAmount();

        if (liters == null && totalAmount == null) {
            throw new BusinessException("Informe litros ou valor total");
        }
        if (liters == null) {
            liters = totalAmount.divide(price, 3, RoundingMode.HALF_UP);
        } else if (totalAmount == null) {
            totalAmount = liters.multiply(price).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal calculated = liters.multiply(price).setScale(2, RoundingMode.HALF_UP);
            if (calculated.subtract(totalAmount).abs().compareTo(TOLERANCE) > 0) {
                throw new BusinessException("Valor total não confere com litros × preço vigente");
            }
        }

        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.paymentMethod());

        var now = LocalDateTime.now();
        var fueling = new Fueling(
                null, shift, nozzle,
                liters, price, totalAmount,
                paymentMethod, FuelingStatus.ACTIVE, null,
                request.vehiclePlate(), request.notes(),
                now, now
        );
        return fuelingRepository.save(fueling);
    }
}
```

- [ ] **Step 5: Ajustar FuelingHandlerTest**

Onde o teste constrói `RegisterFuelingRequest`, usar o novo contrato (sem `unitPrice`), ex.:
```java
new RegisterFuelingRequest(nozzleId, new BigDecimal("10.000"), null, "PIX", null, null)
```

- [ ] **Step 6: Rodar e ver passar**

Run: `cd backend && ./mvnw test`
Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/octane/fueling/ backend/src/test/java/com/octane/fueling/
git commit -m "feat(fueling): price fuelings from current fuel price table"
```

### Task C3: CancelFuelingUseCase + endpoint + lista só ACTIVE

**Files:**
- Create: `backend/src/main/java/com/octane/fueling/usecase/fueling/CancelFuelingUseCase.java`
- Modify: `backend/src/main/java/com/octane/fueling/usecase/fueling/ListFuelingsByShiftUseCase.java`
- Modify: `backend/src/main/java/com/octane/fueling/handler/FuelingHandler.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/fueling/CancelFuelingUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/fueling/ListFuelingsByShiftUseCaseTest.java` (adicionar cenário)
- Test: `backend/src/test/java/com/octane/fueling/handler/FuelingHandlerTest.java` (adicionar teste)

- [ ] **Step 1: Escrever CancelFuelingUseCaseTest (falha)**

```java
package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelFuelingUseCaseTest {

    @Mock
    private FuelingRepository fuelingRepository;

    @InjectMocks
    private CancelFuelingUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Nozzle nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true,
        LocalDateTime.now(), LocalDateTime.now());

    private Fueling buildFueling(Shift shift, FuelingStatus status) {
        return new Fueling(UUID.randomUUID(), shift, nozzle, new BigDecimal("10.000"),
            new BigDecimal("5.00"), new BigDecimal("50.00"), PaymentMethod.PIX,
            status, null, null, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void execute_cancelsFueling_whenShiftOpenAndFuelingActive() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.ACTIVE);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(shift.getId(), fueling.getId());

        assertThat(result.getStatus()).isEqualTo(FuelingStatus.CANCELED);
        assertThat(result.getCanceledAt()).isNotNull();
    }

    @Test
    void execute_throwsBusinessException_whenShiftClosed() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.ACTIVE);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));

        assertThatThrownBy(() -> sut.execute(shift.getId(), fueling.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("aberto");

        verify(fuelingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenAlreadyCanceled() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.CANCELED);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));

        assertThatThrownBy(() -> sut.execute(shift.getId(), fueling.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cancelado");
    }

    @Test
    void execute_throwsEntityNotFound_whenFuelingNotInShift() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.ACTIVE);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));

        assertThatThrownBy(() -> sut.execute(UUID.randomUUID(), fueling.getId()))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_throwsEntityNotFound_whenFuelingMissing() {
        var id = UUID.randomUUID();
        when(fuelingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(UUID.randomUUID(), id))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar, depois implementar**

Run: `cd backend && ./mvnw test -Dtest=CancelFuelingUseCaseTest` → erro de compilação.

`CancelFuelingUseCase.java`:
```java
package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CancelFuelingUseCase {

    private final FuelingRepository fuelingRepository;

    public CancelFuelingUseCase(FuelingRepository fuelingRepository) {
        this.fuelingRepository = fuelingRepository;
    }

    @Transactional
    public Fueling execute(UUID shiftId, UUID fuelingId) {
        var fueling = fuelingRepository.findById(fuelingId)
            .orElseThrow(() -> new EntityNotFoundException("Fueling not found: " + fuelingId));

        if (!fueling.getShift().getId().equals(shiftId)) {
            throw new EntityNotFoundException("Fueling not found in shift: " + fuelingId);
        }

        if (fueling.getStatus() == FuelingStatus.CANCELED) {
            throw new BusinessException("Abastecimento já cancelado");
        }

        if (fueling.getShift().getStatus() != ShiftStatus.OPEN) {
            throw new BusinessException("Turno não está aberto: não é possível cancelar");
        }

        var canceled = new Fueling(fueling.getId(), fueling.getShift(), fueling.getNozzle(),
            fueling.getLiters(), fueling.getUnitPrice(), fueling.getTotalAmount(),
            fueling.getPaymentMethod(), FuelingStatus.CANCELED, LocalDateTime.now(),
            fueling.getVehiclePlate(), fueling.getNotes(), fueling.getFueledAt(),
            fueling.getCreatedAt());
        return fuelingRepository.save(canceled);
    }
}
```

Run: `cd backend && ./mvnw test -Dtest=CancelFuelingUseCaseTest` → 5 PASS.

- [ ] **Step 3: Lista do turno ignora cancelados (teste + filtro)**

Adicionar ao `ListFuelingsByShiftUseCaseTest` um cenário com um fueling ACTIVE e um CANCELED, esperando só o ACTIVE nos itens e nos totais. Implementar em `ListFuelingsByShiftUseCase` filtrando após buscar:
```java
        List<Fueling> fuelings = fuelingRepository.findByShiftId(shiftId).stream()
                .filter(f -> f.getStatus() == FuelingStatus.ACTIVE)
                .toList();
```
(import `com.octane.fueling.domain.FuelingStatus`.)

Run: `cd backend && ./mvnw test -Dtest=ListFuelingsByShiftUseCaseTest` → PASS.

- [ ] **Step 4: Endpoint de cancelamento (teste + handler)**

No `FuelingHandlerTest`, adicionar `@MockitoBean CancelFuelingUseCase cancelFuelingUseCase;` e:
```java
    @Test
    void postCancelFueling_returns200WithCanceledStatus() throws Exception {
        var shiftId = UUID.randomUUID();
        var fuelingId = UUID.randomUUID();
        // construir um Fueling CANCELED com os builders existentes do teste
        when(cancelFuelingUseCase.execute(shiftId, fuelingId)).thenReturn(canceledFueling);

        mockMvc.perform(post("/api/shifts/" + shiftId + "/fuelings/" + fuelingId + "/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(fuelingId.toString()));
    }
```
(`canceledFueling` deve ter `id = fuelingId`.)

No `FuelingHandler`, injetar `CancelFuelingUseCase` e adicionar:
```java
    @PostMapping("/{fuelingId}/cancel")
    public FuelingResponse cancelFueling(@PathVariable UUID shiftId, @PathVariable UUID fuelingId) {
        return FuelingResponse.from(cancelFuelingUseCase.execute(shiftId, fuelingId));
    }
```

Run: `cd backend && ./mvnw test -Dtest=FuelingHandlerTest` → PASS.

- [ ] **Step 5: Suite completa + commit**

Run: `cd backend && ./mvnw test` → BUILD SUCCESS.

```bash
git add backend/src/main/java/com/octane/fueling/ backend/src/test/java/com/octane/fueling/
git commit -m "feat(fueling): add fueling cancellation"
```

---

## Fase D — Conciliação de turno

### Task D1: Migration V10 + entidade ShiftReconciliation + repositório

**Files:**
- Create: `backend/src/main/resources/db/migration/V10__create_shift_reconciliations.sql`
- Create: `backend/src/main/java/com/octane/fueling/domain/ShiftReconciliation.java`
- Modify: `backend/src/main/java/com/octane/fueling/domain/repository/ShiftReconciliationRepository.java` (Create)
- Create: `backend/src/main/java/com/octane/fueling/repository/ShiftReconciliationJpaRepository.java`
- Create: `backend/src/main/java/com/octane/fueling/repository/ShiftReconciliationRepositoryImpl.java`

- [ ] **Step 1: Criar migration**

`V10__create_shift_reconciliations.sql`:
```sql
CREATE TABLE shift_reconciliations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id UUID NOT NULL REFERENCES shifts(id),
    nozzle_id UUID NOT NULL REFERENCES nozzles(id),
    opening_totalizer NUMERIC(12,3) NOT NULL,
    closing_totalizer NUMERIC(12,3) NOT NULL,
    measured_liters NUMERIC(12,3) NOT NULL,
    fueled_liters NUMERIC(12,3) NOT NULL,
    divergence_liters NUMERIC(12,3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (shift_id, nozzle_id)
);
```

- [ ] **Step 2: Criar entidade e repositórios**

`ShiftReconciliation.java`:
```java
package com.octane.fueling.domain;

import com.octane.station.domain.Nozzle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shift_reconciliations")
public class ShiftReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne
    @JoinColumn(name = "nozzle_id", nullable = false)
    private Nozzle nozzle;

    @Column(name = "opening_totalizer", nullable = false, precision = 12, scale = 3)
    private BigDecimal openingTotalizer;

    @Column(name = "closing_totalizer", nullable = false, precision = 12, scale = 3)
    private BigDecimal closingTotalizer;

    @Column(name = "measured_liters", nullable = false, precision = 12, scale = 3)
    private BigDecimal measuredLiters;

    @Column(name = "fueled_liters", nullable = false, precision = 12, scale = 3)
    private BigDecimal fueledLiters;

    @Column(name = "divergence_liters", nullable = false, precision = 12, scale = 3)
    private BigDecimal divergenceLiters;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ShiftReconciliation() {}

    public ShiftReconciliation(UUID id, Shift shift, Nozzle nozzle, BigDecimal openingTotalizer,
                               BigDecimal closingTotalizer, BigDecimal measuredLiters,
                               BigDecimal fueledLiters, BigDecimal divergenceLiters,
                               LocalDateTime createdAt) {
        this.id = id;
        this.shift = shift;
        this.nozzle = nozzle;
        this.openingTotalizer = openingTotalizer;
        this.closingTotalizer = closingTotalizer;
        this.measuredLiters = measuredLiters;
        this.fueledLiters = fueledLiters;
        this.divergenceLiters = divergenceLiters;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Shift getShift() { return shift; }
    public Nozzle getNozzle() { return nozzle; }
    public BigDecimal getOpeningTotalizer() { return openingTotalizer; }
    public BigDecimal getClosingTotalizer() { return closingTotalizer; }
    public BigDecimal getMeasuredLiters() { return measuredLiters; }
    public BigDecimal getFueledLiters() { return fueledLiters; }
    public BigDecimal getDivergenceLiters() { return divergenceLiters; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

`ShiftReconciliationRepository.java` (em `fueling/domain/repository/`):
```java
package com.octane.fueling.domain.repository;

import com.octane.fueling.domain.ShiftReconciliation;

import java.util.List;
import java.util.UUID;

public interface ShiftReconciliationRepository {
    List<ShiftReconciliation> saveAll(List<ShiftReconciliation> reconciliations);
    List<ShiftReconciliation> findByShiftId(UUID shiftId);
}
```

`ShiftReconciliationJpaRepository.java`:
```java
package com.octane.fueling.repository;

import com.octane.fueling.domain.ShiftReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ShiftReconciliationJpaRepository extends JpaRepository<ShiftReconciliation, UUID> {
    List<ShiftReconciliation> findByShift_Id(UUID shiftId);
}
```

`ShiftReconciliationRepositoryImpl.java`:
```java
package com.octane.fueling.repository;

import com.octane.fueling.domain.ShiftReconciliation;
import com.octane.fueling.domain.repository.ShiftReconciliationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ShiftReconciliationRepositoryImpl implements ShiftReconciliationRepository {

    private final ShiftReconciliationJpaRepository jpaRepository;

    public ShiftReconciliationRepositoryImpl(ShiftReconciliationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ShiftReconciliation> saveAll(List<ShiftReconciliation> reconciliations) {
        return jpaRepository.saveAll(reconciliations);
    }

    @Override
    public List<ShiftReconciliation> findByShiftId(UUID shiftId) {
        return jpaRepository.findByShift_Id(shiftId);
    }
}
```

- [ ] **Step 3: Compilar e commitar**

Run: `cd backend && ./mvnw test-compile` → BUILD SUCCESS.

```bash
git add backend/src/main/resources/db/migration/V10__create_shift_reconciliations.sql backend/src/main/java/com/octane/fueling/
git commit -m "feat(fueling): add ShiftReconciliation entity, migration and repository"
```

### Task D2: CloseShiftUseCase persiste conciliação

**Files:**
- Modify: `backend/src/main/java/com/octane/fueling/usecase/shift/CloseShiftUseCase.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/shift/CloseShiftUseCaseTest.java` (adicionar cenários)

- [ ] **Step 1: Adicionar teste que falha**

No `CloseShiftUseCaseTest`, adicionar `@Mock FuelingRepository fuelingRepository;` e `@Mock ShiftReconciliationRepository shiftReconciliationRepository;` (o `@InjectMocks` injeta os novos parâmetros do construtor). Adicionar cenário (ajustar aos builders existentes do teste — ele já monta posto/bomba/bico ativos e leituras CLOSING para o caminho feliz):

```java
    @Test
    void execute_persistsReconciliationPerNozzle_onClose() {
        // arrange: caminho feliz existente com 1 bico ativo,
        // leitura OPENING totalizer=1000.000 e CLOSING totalizer=1100.000,
        // mais 1 fueling ACTIVE de 95.500 L e 1 fueling CANCELED de 10.000 L no mesmo bico
        when(fuelingRepository.findByShiftId(shift.getId())).thenReturn(List.of(activeFueling, canceledFueling));
        when(shiftReconciliationRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(shift.getId());

        ArgumentCaptor<List<ShiftReconciliation>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftReconciliationRepository).saveAll(captor.capture());
        var lines = captor.getValue();
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getMeasuredLiters()).isEqualByComparingTo("100.000");
        assertThat(lines.get(0).getFueledLiters()).isEqualByComparingTo("95.500");
        assertThat(lines.get(0).getDivergenceLiters()).isEqualByComparingTo("4.500");
    }
```

Regras a cobrir (mais um teste): bico sem leitura OPENING (só CLOSING) **não** gera linha.

- [ ] **Step 2: Rodar e ver falhar**

Run: `cd backend && ./mvnw test -Dtest=CloseShiftUseCaseTest`
Expected: erro de compilação (construtor não recebe os repos novos).

- [ ] **Step 3: Implementar**

No `CloseShiftUseCase`: adicionar `FuelingRepository fuelingRepository` e `ShiftReconciliationRepository shiftReconciliationRepository` como campos `private final` + parâmetros do construtor. Após a validação de leituras e **antes** do `return`, inserir:

```java
        var readings = nozzleReadingRepository.findByShiftId(shiftId);
        Map<UUID, NozzleReading> openingByNozzle = readings.stream()
                .filter(r -> r.getType() == NozzleReadingType.OPENING)
                .collect(Collectors.toMap(r -> r.getNozzle().getId(), r -> r));
        Map<UUID, NozzleReading> closingByNozzle = readings.stream()
                .filter(r -> r.getType() == NozzleReadingType.CLOSING)
                .collect(Collectors.toMap(r -> r.getNozzle().getId(), r -> r));

        Map<UUID, BigDecimal> fueledByNozzle = fuelingRepository.findByShiftId(shiftId).stream()
                .filter(f -> f.getStatus() == FuelingStatus.ACTIVE)
                .collect(Collectors.groupingBy(f -> f.getNozzle().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Fueling::getLiters, BigDecimal::add)));

        var now = LocalDateTime.now();
        List<ShiftReconciliation> reconciliations = closingByNozzle.entrySet().stream()
                .filter(entry -> openingByNozzle.containsKey(entry.getKey()))
                .map(entry -> {
                    var opening = openingByNozzle.get(entry.getKey());
                    var closing = entry.getValue();
                    var measured = closing.getTotalizer().subtract(opening.getTotalizer());
                    var fueled = fueledByNozzle.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                    return new ShiftReconciliation(null, shift, closing.getNozzle(),
                            opening.getTotalizer(), closing.getTotalizer(),
                            measured, fueled, measured.subtract(fueled), now);
                })
                .toList();
        shiftReconciliationRepository.saveAll(reconciliations);
```

Imports novos: `Fueling`, `FuelingStatus`, `NozzleReading`, `ShiftReconciliation`, `FuelingRepository`, `ShiftReconciliationRepository`, `java.math.BigDecimal`, `java.util.Map`, `java.util.stream.Collectors`.

Nota: a variável `closingReadings` já existente pode ser substituída por `readings` (mesma query) — não duplicar a chamada a `findByShiftId`.

- [ ] **Step 4: Rodar e ver passar**

Run: `cd backend && ./mvnw test`
Expected: BUILD SUCCESS (ajustar `ShiftHandlerTest` se o mock do construtor reclamar — handler não muda nesta task).

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/octane/fueling/usecase/shift/CloseShiftUseCase.java backend/src/test/java/com/octane/fueling/usecase/shift/CloseShiftUseCaseTest.java
git commit -m "feat(fueling): persist shift reconciliation on close"
```

### Task D3: GET /api/shifts/{id}/reconciliation

**Files:**
- Create: `backend/src/main/java/com/octane/fueling/usecase/shift/ReconciliationLineResponse.java`
- Create: `backend/src/main/java/com/octane/fueling/usecase/shift/ShiftReconciliationResponse.java`
- Create: `backend/src/main/java/com/octane/fueling/usecase/shift/GetShiftReconciliationUseCase.java`
- Modify: `backend/src/main/java/com/octane/fueling/handler/ShiftHandler.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/shift/GetShiftReconciliationUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/fueling/handler/ShiftHandlerTest.java` (adicionar teste)

- [ ] **Step 1: Escrever o teste do use case (falha)**

```java
package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftReconciliation;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftReconciliationRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShiftReconciliationUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private ShiftReconciliationRepository shiftReconciliationRepository;

    @InjectMocks
    private GetShiftReconciliationUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Nozzle nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true,
        LocalDateTime.now(), LocalDateTime.now());

    @Test
    void execute_returnsLinesAndTotals_whenShiftClosed() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now());
        var line = new ShiftReconciliation(UUID.randomUUID(), shift, nozzle,
            new BigDecimal("1000.000"), new BigDecimal("1100.000"),
            new BigDecimal("100.000"), new BigDecimal("95.500"), new BigDecimal("4.500"),
            LocalDateTime.now());

        when(shiftRepository.findById(shift.getId())).thenReturn(Optional.of(shift));
        when(shiftReconciliationRepository.findByShiftId(shift.getId())).thenReturn(List.of(line));

        var result = sut.execute(shift.getId());

        assertThat(result.lines()).hasSize(1);
        assertThat(result.lines().get(0).nozzleNumber()).isEqualTo(1);
        assertThat(result.lines().get(0).fuelName()).isEqualTo("Gasolina Comum");
        assertThat(result.totalMeasuredLiters()).isEqualByComparingTo("100.000");
        assertThat(result.totalFueledLiters()).isEqualByComparingTo("95.500");
        assertThat(result.totalDivergenceLiters()).isEqualByComparingTo("4.500");
    }

    @Test
    void execute_throwsBusinessException_whenShiftStillOpen() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        when(shiftRepository.findById(shift.getId())).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> sut.execute(shift.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("fechado");
    }

    @Test
    void execute_throwsEntityNotFound_whenShiftMissing() {
        var id = UUID.randomUUID();
        when(shiftRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

- [ ] **Step 2: Rodar e ver falhar, depois implementar**

Run: `cd backend && ./mvnw test -Dtest=GetShiftReconciliationUseCaseTest` → erro de compilação.

`ReconciliationLineResponse.java`:
```java
package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.ShiftReconciliation;

import java.math.BigDecimal;
import java.util.UUID;

public record ReconciliationLineResponse(
    UUID nozzleId,
    int nozzleNumber,
    String fuelName,
    BigDecimal openingTotalizer,
    BigDecimal closingTotalizer,
    BigDecimal measuredLiters,
    BigDecimal fueledLiters,
    BigDecimal divergenceLiters
) {
    public static ReconciliationLineResponse from(ShiftReconciliation reconciliation) {
        return new ReconciliationLineResponse(
            reconciliation.getNozzle().getId(),
            reconciliation.getNozzle().getNumber(),
            reconciliation.getNozzle().getFuel().getName(),
            reconciliation.getOpeningTotalizer(),
            reconciliation.getClosingTotalizer(),
            reconciliation.getMeasuredLiters(),
            reconciliation.getFueledLiters(),
            reconciliation.getDivergenceLiters()
        );
    }
}
```

`ShiftReconciliationResponse.java`:
```java
package com.octane.fueling.usecase.shift;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShiftReconciliationResponse(
    UUID shiftId,
    List<ReconciliationLineResponse> lines,
    BigDecimal totalMeasuredLiters,
    BigDecimal totalFueledLiters,
    BigDecimal totalDivergenceLiters
) {}
```

`GetShiftReconciliationUseCase.java`:
```java
package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftReconciliationRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GetShiftReconciliationUseCase {

    private final ShiftRepository shiftRepository;
    private final ShiftReconciliationRepository shiftReconciliationRepository;

    public GetShiftReconciliationUseCase(ShiftRepository shiftRepository,
                                         ShiftReconciliationRepository shiftReconciliationRepository) {
        this.shiftRepository = shiftRepository;
        this.shiftReconciliationRepository = shiftReconciliationRepository;
    }

    public ShiftReconciliationResponse execute(UUID shiftId) {
        var shift = shiftRepository.findById(shiftId)
            .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + shiftId));

        if (shift.getStatus() != ShiftStatus.CLOSED) {
            throw new BusinessException("Conciliação disponível apenas para turno fechado");
        }

        var reconciliations = shiftReconciliationRepository.findByShiftId(shiftId);

        var lines = reconciliations.stream().map(ReconciliationLineResponse::from).toList();
        var totalMeasured = reconciliations.stream()
            .map(r -> r.getMeasuredLiters()).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalFueled = reconciliations.stream()
            .map(r -> r.getFueledLiters()).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalDivergence = reconciliations.stream()
            .map(r -> r.getDivergenceLiters()).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShiftReconciliationResponse(shiftId, lines, totalMeasured, totalFueled, totalDivergence);
    }
}
```

Run: `cd backend && ./mvnw test -Dtest=GetShiftReconciliationUseCaseTest` → 3 PASS.

- [ ] **Step 3: Endpoint (teste + handler)**

No `ShiftHandlerTest`, adicionar `@MockitoBean GetShiftReconciliationUseCase getShiftReconciliationUseCase;` e:
```java
    @Test
    void getReconciliation_returns200WithTotals() throws Exception {
        var shiftId = UUID.randomUUID();
        var response = new ShiftReconciliationResponse(shiftId, List.of(),
            new BigDecimal("100.000"), new BigDecimal("95.500"), new BigDecimal("4.500"));
        when(getShiftReconciliationUseCase.execute(shiftId)).thenReturn(response);

        mockMvc.perform(get("/api/shifts/" + shiftId + "/reconciliation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalDivergenceLiters").value(4.500));
    }
```

No `ShiftHandler`, injetar `GetShiftReconciliationUseCase` e adicionar:
```java
    @GetMapping("/api/shifts/{id}/reconciliation")
    public ShiftReconciliationResponse reconciliation(@PathVariable UUID id) {
        return getShiftReconciliationUseCase.execute(id);
    }
```

Run: `cd backend && ./mvnw test -Dtest=ShiftHandlerTest` → PASS.

- [ ] **Step 4: Suite completa + commit**

Run: `cd backend && ./mvnw test` → BUILD SUCCESS.

```bash
git add backend/src/main/java/com/octane/fueling/ backend/src/test/java/com/octane/fueling/
git commit -m "feat(fueling): add shift reconciliation report endpoint"
```

---

## Fase E — Paginação e filtros

### Task E1: PageResponse genérico em shared

**Files:**
- Create: `backend/src/main/java/com/octane/shared/pagination/PageResponse.java`
- Test: `backend/src/test/java/com/octane/shared/pagination/PageResponseTest.java`

- [ ] **Step 1: Teste (falha)**

```java
package com.octane.shared.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void of_computesTotalPages() {
        var page = PageResponse.of(List.of("a", "b"), 0, 2, 5);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.content()).containsExactly("a", "b");
    }

    @Test
    void map_transformsContentKeepingMetadata() {
        var page = PageResponse.of(List.of(1, 2), 1, 2, 4);

        var mapped = page.map(String::valueOf);

        assertThat(mapped.content()).containsExactly("1", "2");
        assertThat(mapped.page()).isEqualTo(1);
        assertThat(mapped.totalElements()).isEqualTo(4);
        assertThat(mapped.totalPages()).isEqualTo(2);
    }
}
```

- [ ] **Step 2: Implementar e rodar**

`PageResponse.java`:
```java
package com.octane.shared.pagination;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    public <R> PageResponse<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResponse<>(content.stream().<R>map(mapper).toList(),
            page, size, totalElements, totalPages);
    }
}
```

Run: `cd backend && ./mvnw test -Dtest=PageResponseTest` → 2 PASS.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/octane/shared/pagination/ backend/src/test/java/com/octane/shared/pagination/
git commit -m "feat(shared): add generic PageResponse"
```

### Task E2: Turnos paginados com filtros (status, período)

**Files:**
- Modify: `backend/src/main/java/com/octane/fueling/domain/repository/ShiftRepository.java`
- Modify: `backend/src/main/java/com/octane/fueling/repository/ShiftJpaRepository.java`
- Modify: `backend/src/main/java/com/octane/fueling/repository/ShiftRepositoryImpl.java`
- Modify: `backend/src/main/java/com/octane/fueling/usecase/shift/ListShiftsByStationUseCase.java`
- Modify: `backend/src/main/java/com/octane/fueling/handler/ShiftHandler.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/shift/ListShiftsByStationUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/fueling/handler/ShiftHandlerTest.java`

- [ ] **Step 1: Mudar contrato do domínio**

Em `ShiftRepository`, **substituir** `List<Shift> findByStationId(UUID stationId);` por:
```java
    PageResponse<Shift> findByStationId(UUID stationId, ShiftStatus status,
                                        LocalDateTime from, LocalDateTime to,
                                        int page, int size);
```
Imports: `com.octane.shared.pagination.PageResponse`, `com.octane.fueling.domain.ShiftStatus`, `java.time.LocalDateTime`.

- [ ] **Step 2: Implementação com Specification**

`ShiftJpaRepository` passa a estender também `JpaSpecificationExecutor<Shift>`:
```java
interface ShiftJpaRepository extends JpaRepository<Shift, UUID>, JpaSpecificationExecutor<Shift> {
    Optional<Shift> findByStation_IdAndStatus(UUID stationId, ShiftStatus status);
}
```
(remover `findByStation_Id` — não será mais usado; import `org.springframework.data.jpa.repository.JpaSpecificationExecutor`.)

Em `ShiftRepositoryImpl`, substituir o método antigo por:
```java
    @Override
    public PageResponse<Shift> findByStationId(UUID stationId, ShiftStatus status,
                                               LocalDateTime from, LocalDateTime to,
                                               int page, int size) {
        Specification<Shift> spec = (root, query, cb) -> cb.equal(root.get("station").get("id"), stationId);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("openedAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("openedAt"), to));
        }
        Page<Shift> result = jpaRepository.findAll(spec,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "openedAt")));
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }
```
Imports: `PageResponse`, `ShiftStatus` (já há), `org.springframework.data.domain.Page`, `org.springframework.data.domain.PageRequest`, `org.springframework.data.domain.Sort`, `org.springframework.data.jpa.domain.Specification`, `java.time.LocalDateTime`.

- [ ] **Step 3: Use case (teste primeiro)**

Reescrever `ListShiftsByStationUseCaseTest` para o novo contrato:
```java
    @Test
    void execute_returnsPagedShifts_parsingStatus() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(shiftRepository.findByStationId(stationId, ShiftStatus.OPEN, null, null, 0, 20))
            .thenReturn(PageResponse.of(List.of(shift), 0, 20, 1));

        var result = sut.execute(stationId, "OPEN", null, null, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId, null, null, null, 0, 20))
            .isInstanceOf(EntityNotFoundException.class);
    }
```

Implementação do `ListShiftsByStationUseCase`:
```java
    public PageResponse<Shift> execute(UUID stationId, String status,
                                       LocalDateTime from, LocalDateTime to,
                                       int page, int size) {
        stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));
        ShiftStatus shiftStatus = status != null ? ShiftStatus.valueOf(status) : null;
        return shiftRepository.findByStationId(stationId, shiftStatus, from, to, page, size);
    }
```

- [ ] **Step 4: Handler**

Em `ShiftHandler.listByStation`:
```java
    @GetMapping("/api/stations/{stationId}/shifts")
    public PageResponse<ShiftResponse> listByStation(
        @PathVariable UUID stationId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return listShiftsByStationUseCase.execute(stationId, status, from, to, page, size)
            .map(ShiftResponse::from);
    }
```
Imports: `PageResponse`, `RequestParam`, `org.springframework.format.annotation.DateTimeFormat`, `java.time.LocalDateTime`.

No `ShiftHandlerTest`, ajustar o teste de listagem:
```java
    @Test
    void getShiftsByStation_returns200WithPage() throws Exception {
        var stationId = UUID.randomUUID();
        // shift construído como no teste atual
        when(listShiftsByStationUseCase.execute(eq(stationId), eq(null), eq(null), eq(null), eq(0), eq(20)))
            .thenReturn(PageResponse.of(List.of(shift), 0, 20, 1));

        mockMvc.perform(get("/api/stations/" + stationId + "/shifts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].employeeName").value("João"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }
```

- [ ] **Step 5: Rodar suite e commitar**

Run: `cd backend && ./mvnw test` → BUILD SUCCESS.

```bash
git add backend/src/main/java/com/octane/fueling/ backend/src/test/java/com/octane/fueling/
git commit -m "feat(fueling): paginate and filter shifts by station"
```

### Task E3: Abastecimentos paginados + totais agregados

**Files:**
- Modify: `backend/src/main/java/com/octane/fueling/domain/repository/FuelingRepository.java`
- Modify: `backend/src/main/java/com/octane/fueling/repository/FuelingJpaRepository.java`
- Modify: `backend/src/main/java/com/octane/fueling/repository/FuelingRepositoryImpl.java`
- Modify: `backend/src/main/java/com/octane/fueling/usecase/fueling/ShiftSummaryResponse.java`
- Modify: `backend/src/main/java/com/octane/fueling/usecase/fueling/ListFuelingsByShiftUseCase.java`
- Modify: `backend/src/main/java/com/octane/fueling/handler/FuelingHandler.java`
- Test: `backend/src/test/java/com/octane/fueling/usecase/fueling/ListFuelingsByShiftUseCaseTest.java`
- Test: `backend/src/test/java/com/octane/fueling/handler/FuelingHandlerTest.java`

- [ ] **Step 1: Contrato do domínio**

Em `FuelingRepository`, adicionar (mantendo `findByShiftId` — usado pela conciliação):
```java
    PageResponse<Fueling> findByShiftIdAndStatus(UUID shiftId, FuelingStatus status, int page, int size);
    BigDecimal sumLitersByShiftIdAndStatus(UUID shiftId, FuelingStatus status);
    BigDecimal sumTotalAmountByShiftIdAndStatus(UUID shiftId, FuelingStatus status);
```
Imports: `PageResponse`, `FuelingStatus`, `java.math.BigDecimal`.

- [ ] **Step 2: Implementação JPA**

`FuelingJpaRepository`:
```java
    Page<Fueling> findByShift_IdAndStatus(UUID shiftId, FuelingStatus status, Pageable pageable);

    @Query("select coalesce(sum(f.liters), 0) from Fueling f where f.shift.id = :shiftId and f.status = :status")
    BigDecimal sumLiters(@Param("shiftId") UUID shiftId, @Param("status") FuelingStatus status);

    @Query("select coalesce(sum(f.totalAmount), 0) from Fueling f where f.shift.id = :shiftId and f.status = :status")
    BigDecimal sumTotalAmount(@Param("shiftId") UUID shiftId, @Param("status") FuelingStatus status);
```
Imports: `FuelingStatus`, `org.springframework.data.domain.Page`, `org.springframework.data.domain.Pageable`, `org.springframework.data.jpa.repository.Query`, `org.springframework.data.repository.query.Param`, `java.math.BigDecimal`.

`FuelingRepositoryImpl`:
```java
    @Override
    public PageResponse<Fueling> findByShiftIdAndStatus(UUID shiftId, FuelingStatus status, int page, int size) {
        var result = jpaRepository.findByShift_IdAndStatus(shiftId, status,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fueledAt")));
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }

    @Override
    public BigDecimal sumLitersByShiftIdAndStatus(UUID shiftId, FuelingStatus status) {
        return jpaRepository.sumLiters(shiftId, status);
    }

    @Override
    public BigDecimal sumTotalAmountByShiftIdAndStatus(UUID shiftId, FuelingStatus status) {
        return jpaRepository.sumTotalAmount(shiftId, status);
    }
```

- [ ] **Step 3: Use case e response (teste primeiro)**

`ShiftSummaryResponse.java` (substituir):
```java
package com.octane.fueling.usecase.fueling;

import com.octane.shared.pagination.PageResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record ShiftSummaryResponse(
    UUID shiftId,
    PageResponse<FuelingResponse> fuelings,
    BigDecimal totalLiters,
    BigDecimal totalAmount
) {}
```

Reescrever `ListFuelingsByShiftUseCaseTest` para o novo contrato (mock dos 3 métodos novos, totais vindos das queries agregadas — não da página):
```java
    @Test
    void execute_returnsPageAndAggregatedTotals() {
        // arrange shift + fueling ACTIVE como no teste atual
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(fuelingRepository.findByShiftIdAndStatus(shiftId, FuelingStatus.ACTIVE, 0, 20))
            .thenReturn(PageResponse.of(List.of(fueling), 0, 20, 35));
        when(fuelingRepository.sumLitersByShiftIdAndStatus(shiftId, FuelingStatus.ACTIVE))
            .thenReturn(new BigDecimal("350.000"));
        when(fuelingRepository.sumTotalAmountByShiftIdAndStatus(shiftId, FuelingStatus.ACTIVE))
            .thenReturn(new BigDecimal("1750.00"));

        var result = sut.execute(shiftId, 0, 20);

        assertThat(result.fuelings().content()).hasSize(1);
        assertThat(result.fuelings().totalElements()).isEqualTo(35);
        assertThat(result.totalLiters()).isEqualByComparingTo("350.000");
        assertThat(result.totalAmount()).isEqualByComparingTo("1750.00");
    }
```

`ListFuelingsByShiftUseCase` (substituir corpo):
```java
    public ShiftSummaryResponse execute(UUID shiftId, int page, int size) {
        shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + shiftId));

        var fuelings = fuelingRepository
                .findByShiftIdAndStatus(shiftId, FuelingStatus.ACTIVE, page, size)
                .map(FuelingResponse::from);

        return new ShiftSummaryResponse(
                shiftId,
                fuelings,
                fuelingRepository.sumLitersByShiftIdAndStatus(shiftId, FuelingStatus.ACTIVE),
                fuelingRepository.sumTotalAmountByShiftIdAndStatus(shiftId, FuelingStatus.ACTIVE)
        );
    }
```

- [ ] **Step 4: Handler**

`FuelingHandler.listFuelings`:
```java
    @GetMapping
    public ShiftSummaryResponse listFuelings(@PathVariable UUID shiftId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return listFuelingsByShiftUseCase.execute(shiftId, page, size);
    }
```
Ajustar `FuelingHandlerTest` (`execute(shiftId, 0, 20)` no mock; asserts em `$.fuelings.content[0]...`).

- [ ] **Step 5: Rodar suite e commitar**

Run: `cd backend && ./mvnw test` → BUILD SUCCESS.

```bash
git add backend/src/main/java/com/octane/fueling/ backend/src/test/java/com/octane/fueling/
git commit -m "feat(fueling): paginate shift fuelings with aggregated totals"
```

### Task E4: Filtros nos cadastros (?active= / ?status=)

**Files:**
- Modify: `backend/src/main/java/com/octane/station/usecase/station/ListStationsUseCase.java`
- Modify: `backend/src/main/java/com/octane/station/usecase/pump/ListPumpsByStationUseCase.java`
- Modify: `backend/src/main/java/com/octane/station/usecase/nozzle/ListNozzlesByPumpUseCase.java`
- Modify: `backend/src/main/java/com/octane/station/usecase/fuel/ListFuelsUseCase.java`
- Modify: handlers correspondentes (`StationHandler`, `PumpHandler`, `FuelHandler`)
- Test: testes dos 4 use cases + handlers (adicionar cenário com filtro)

- [ ] **Step 1: Padrão (igual para os quatro) — teste primeiro**

Exemplo para `ListStationsUseCase` (replicar a ideia nos demais):
```java
    @Test
    void execute_filtersByActive_whenParamGiven() {
        var active = buildStation(UUID.randomUUID(), true);
        var inactive = buildStation(UUID.randomUUID(), false);
        when(stationRepository.findAll()).thenReturn(List.of(active, inactive));

        var result = sut.execute(false);

        assertThat(result).containsExactly(inactive);
    }

    @Test
    void execute_returnsAll_whenParamNull() {
        var active = buildStation(UUID.randomUUID(), true);
        var inactive = buildStation(UUID.randomUUID(), false);
        when(stationRepository.findAll()).thenReturn(List.of(active, inactive));

        assertThat(sut.execute(null)).hasSize(2);
    }
```

- [ ] **Step 2: Implementar os quatro use cases**

`ListStationsUseCase`:
```java
    public List<Station> execute(Boolean active) {
        return stationRepository.findAll().stream()
            .filter(station -> active == null || station.isActive() == active)
            .toList();
    }
```

`ListPumpsByStationUseCase` (assinatura `execute(UUID stationId, String status)`; manter validação de station existente se já houver):
```java
    public List<Pump> execute(UUID stationId, String status) {
        PumpStatus pumpStatus = status != null ? PumpStatus.valueOf(status) : null;
        return pumpRepository.findByStationId(stationId).stream()
            .filter(pump -> pumpStatus == null || pump.getStatus() == pumpStatus)
            .toList();
    }
```

`ListNozzlesByPumpUseCase` (assinatura `execute(UUID pumpId, Boolean active)`):
```java
    public List<Nozzle> execute(UUID pumpId, Boolean active) {
        return nozzleRepository.findByPumpId(pumpId).stream()
            .filter(nozzle -> active == null || nozzle.isActive() == active)
            .toList();
    }
```

`ListFuelsUseCase` (assinatura `execute(Boolean active)`):
```java
    public List<Fuel> execute(Boolean active) {
        return fuelRepository.findAll().stream()
            .filter(fuel -> active == null || fuel.isActive() == active)
            .toList();
    }
```

- [ ] **Step 3: Handlers — adicionar `@RequestParam(required = false)`**

```java
    // StationHandler
    @GetMapping
    public List<StationResponse> list(@RequestParam(required = false) Boolean active) { ... }

    // StationHandler (bombas do posto)
    @GetMapping("/{id}/pumps")
    public List<PumpResponse> listPumps(@PathVariable UUID id,
                                        @RequestParam(required = false) String status) { ... }

    // PumpHandler (bicos da bomba)
    @GetMapping("/{id}/nozzles")
    public List<NozzleResponse> listNozzles(@PathVariable UUID id,
                                            @RequestParam(required = false) Boolean active) { ... }

    // FuelHandler
    @GetMapping
    public List<FuelResponse> list(@RequestParam(required = false) Boolean active) { ... }
```
Ajustar chamadas internas e os testes de handler existentes para os novos argumentos (`execute(null)` etc.), e adicionar um teste com query param por handler, ex.: `get("/api/stations?active=true")`.

- [ ] **Step 4: Rodar suite e commitar**

Run: `cd backend && ./mvnw test` → BUILD SUCCESS.

```bash
git add backend/src/main/java/com/octane/station/ backend/src/test/java/com/octane/station/
git commit -m "feat(station): add active/status filters to listing endpoints"
```

### Task E5: Verificação final + documentação

**Files:**
- Modify: `README.md` (tabela de endpoints)

- [ ] **Step 1: Suite completa**

Run: `cd backend && ./mvnw test`
Expected: BUILD SUCCESS, zero falhas.

- [ ] **Step 2: Subir o stack e validar o fluxo a mão (smoke test)**

```bash
make dev-db
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
Fluxo: criar posto → bomba → bico → preço (POST /prices) → abrir turno → leitura OPENING → abastecer informando só litros (conferir totalAmount calculado) → cancelar um abastecimento → leitura CLOSING → fechar turno → GET /reconciliation (conferir divergência) → GET /shifts paginado.

- [ ] **Step 3: Atualizar README**

Substituir a tabela de endpoints do `README.md` pela lista completa atual (cadastros, preços, turnos, leituras, abastecimentos, cancelamento, conciliação, filtros e paginação).

- [ ] **Step 4: Commit final**

```bash
git add README.md
git commit -m "docs: update endpoint table with pricing, reconciliation and filters"
```

---

## Self-review (do plano)

- **Cobertura da spec:** seção 1 → Fase B; seção 2 → Fase C; seção 3 → Fase D; seção 4 → Fase A; seção 5 → Fase E. Validação "posto ativo ao abrir turno" → Task A8. Conciliação cobre bico inativado no meio do turno (par OPENING+CLOSING) → Task D2.
- **Tipos consistentes:** construtor novo de `Fueling` definido em C1 e usado em C2/C3/D2; `PageResponse` definido em E1 e usado em E2/E3; `FuelingStatus` definido em C1 e usado em C2/C3/D2/E3.
- **Decisões registradas:** `findByShiftId` permanece no `FuelingRepository` (uso da conciliação); `findByStationId(UUID)` simples sai do `ShiftRepository` (substituído pela versão paginada).
