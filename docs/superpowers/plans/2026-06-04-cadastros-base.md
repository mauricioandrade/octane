# Cadastros Base Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the base registrations module (Station, Fuel, Pump, Nozzle) following Clean Architecture with domain interfaces, repository implementations, use cases, and REST handlers.

**Architecture:** Domain layer holds pure Java interfaces and JPA entities in `station/domain/`; infrastructure implementations live in `station/repository/`; business logic lives in `station/usecase/`; REST handlers in `station/handler/`. No Lombok, constructor injection only.

**Tech Stack:** Java 21 (Records for DTOs), Spring Boot 4.0.6, Spring Data JPA, Flyway, PostgreSQL, JUnit 5 + Mockito, `@WebMvcTest` from `spring-boot-webmvc-test`

---

## File Map

### Created
- `backend/src/main/resources/db/migration/V1__create_stations.sql`
- `backend/src/main/resources/db/migration/V2__create_fuels.sql`
- `backend/src/main/resources/db/migration/V3__create_pumps.sql`
- `backend/src/main/resources/db/migration/V4__create_nozzles.sql`
- `backend/src/main/java/com/octane/shared/exception/EntityNotFoundException.java`
- `backend/src/main/java/com/octane/shared/exception/BusinessException.java`
- `backend/src/main/java/com/octane/shared/exception/ErrorResponse.java`
- `backend/src/main/java/com/octane/shared/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/octane/shared/exception/GlobalExceptionHandlerTest.java`
- `backend/src/main/java/com/octane/station/domain/FuelUnit.java`
- `backend/src/main/java/com/octane/station/domain/PumpStatus.java`
- `backend/src/main/java/com/octane/station/domain/Station.java`
- `backend/src/main/java/com/octane/station/domain/Fuel.java`
- `backend/src/main/java/com/octane/station/domain/Pump.java`
- `backend/src/main/java/com/octane/station/domain/Nozzle.java`
- `backend/src/main/java/com/octane/station/domain/repository/StationRepository.java`
- `backend/src/main/java/com/octane/station/domain/repository/FuelRepository.java`
- `backend/src/main/java/com/octane/station/domain/repository/PumpRepository.java`
- `backend/src/main/java/com/octane/station/domain/repository/NozzleRepository.java`
- `backend/src/main/java/com/octane/station/repository/StationJpaRepository.java`
- `backend/src/main/java/com/octane/station/repository/StationRepositoryImpl.java`
- `backend/src/main/java/com/octane/station/repository/FuelJpaRepository.java`
- `backend/src/main/java/com/octane/station/repository/FuelRepositoryImpl.java`
- `backend/src/main/java/com/octane/station/repository/PumpJpaRepository.java`
- `backend/src/main/java/com/octane/station/repository/PumpRepositoryImpl.java`
- `backend/src/main/java/com/octane/station/repository/NozzleJpaRepository.java`
- `backend/src/main/java/com/octane/station/repository/NozzleRepositoryImpl.java`
- `backend/src/main/java/com/octane/station/usecase/station/CreateStationRequest.java`
- `backend/src/main/java/com/octane/station/usecase/station/CreateStationUseCase.java`
- `backend/src/main/java/com/octane/station/usecase/station/FindStationUseCase.java`
- `backend/src/main/java/com/octane/station/usecase/station/ListStationsUseCase.java`
- `backend/src/test/java/com/octane/station/usecase/station/CreateStationUseCaseTest.java`
- `backend/src/test/java/com/octane/station/usecase/station/FindStationUseCaseTest.java`
- `backend/src/main/java/com/octane/station/usecase/pump/CreatePumpRequest.java`
- `backend/src/main/java/com/octane/station/usecase/pump/CreatePumpUseCase.java`
- `backend/src/main/java/com/octane/station/usecase/pump/ListPumpsByStationUseCase.java`
- `backend/src/test/java/com/octane/station/usecase/pump/CreatePumpUseCaseTest.java`
- `backend/src/main/java/com/octane/station/usecase/nozzle/CreateNozzleRequest.java`
- `backend/src/main/java/com/octane/station/usecase/nozzle/CreateNozzleUseCase.java`
- `backend/src/main/java/com/octane/station/usecase/nozzle/ListNozzlesByPumpUseCase.java`
- `backend/src/test/java/com/octane/station/usecase/nozzle/CreateNozzleUseCaseTest.java`
- `backend/src/main/java/com/octane/station/usecase/fuel/ListFuelsUseCase.java`
- `backend/src/main/java/com/octane/station/handler/StationResponse.java`
- `backend/src/main/java/com/octane/station/handler/PumpResponse.java`
- `backend/src/main/java/com/octane/station/handler/NozzleResponse.java`
- `backend/src/main/java/com/octane/station/handler/FuelResponse.java`
- `backend/src/main/java/com/octane/station/handler/StationHandler.java`
- `backend/src/main/java/com/octane/station/handler/PumpHandler.java`
- `backend/src/main/java/com/octane/station/handler/NozzleHandler.java`
- `backend/src/main/java/com/octane/station/handler/FuelHandler.java`
- `backend/src/test/java/com/octane/station/handler/StationHandlerTest.java`
- `backend/src/test/java/com/octane/station/handler/PumpHandlerTest.java`
- `backend/src/test/java/com/octane/station/handler/FuelHandlerTest.java`

### Modified
- `backend/src/main/resources/db/migration/.gitkeep` — replaced by V1-V4 migration files
- `backend/src/main/java/com/octane/station/domain/.gitkeep` — replaced by domain files
- `backend/src/main/java/com/octane/station/handler/.gitkeep` — replaced by handler files
- `backend/src/main/java/com/octane/station/repository/.gitkeep` — replaced by impl files
- `backend/src/main/java/com/octane/station/usecase/.gitkeep` — replaced by use case files
- `backend/src/main/java/com/octane/shared/exception/.gitkeep` — replaced by exception files

---

## Task 1: Flyway Migrations

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__create_stations.sql`
- Create: `backend/src/main/resources/db/migration/V2__create_fuels.sql`
- Create: `backend/src/main/resources/db/migration/V3__create_pumps.sql`
- Create: `backend/src/main/resources/db/migration/V4__create_nozzles.sql`
- Delete: `backend/src/main/resources/db/migration/.gitkeep`

- [ ] **Step 1: Create V1__create_stations.sql**

```sql
CREATE TABLE stations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state CHAR(2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

- [ ] **Step 2: Create V2__create_fuels.sql**

```sql
CREATE TABLE fuels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    unit VARCHAR(10) NOT NULL DEFAULT 'LITER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO fuels (name, unit) VALUES
    ('Gasolina Comum', 'LITER'),
    ('Gasolina Aditivada', 'LITER'),
    ('Etanol', 'LITER'),
    ('Diesel S10', 'LITER'),
    ('Diesel S500', 'LITER');
```

- [ ] **Step 3: Create V3__create_pumps.sql**

```sql
CREATE TABLE pumps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    station_id UUID NOT NULL REFERENCES stations(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(station_id, number)
);
```

- [ ] **Step 4: Create V4__create_nozzles.sql**

```sql
CREATE TABLE nozzles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number INTEGER NOT NULL,
    pump_id UUID NOT NULL REFERENCES pumps(id),
    fuel_id UUID NOT NULL REFERENCES fuels(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(pump_id, number)
);
```

- [ ] **Step 5: Delete the .gitkeep placeholder**

```bash
rm backend/src/main/resources/db/migration/.gitkeep
```

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/resources/db/migration/
git commit -m "feat(backend): add Flyway migrations for stations, fuels, pumps, nozzles"
```

---

## Task 2: Shared Exceptions + GlobalExceptionHandler

**Files:**
- Create: `backend/src/main/java/com/octane/shared/exception/EntityNotFoundException.java`
- Create: `backend/src/main/java/com/octane/shared/exception/BusinessException.java`
- Create: `backend/src/main/java/com/octane/shared/exception/ErrorResponse.java`
- Create: `backend/src/main/java/com/octane/shared/exception/GlobalExceptionHandler.java`
- Create: `backend/src/test/java/com/octane/shared/exception/GlobalExceptionHandlerTest.java`
- Delete: `backend/src/main/java/com/octane/shared/exception/.gitkeep`

- [ ] **Step 1: Write the failing test for GlobalExceptionHandler**

Create `backend/src/test/java/com/octane/shared/exception/GlobalExceptionHandlerTest.java`:

```java
package com.octane.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        void notFound() {
            throw new EntityNotFoundException("entity not found");
        }

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException("business rule violated");
        }
    }

    @Test
    void entityNotFoundException_returns404WithMessage() throws Exception {
        mockMvc.perform(get("/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("entity not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void businessException_returns422WithMessage() throws Exception {
        mockMvc.perform(get("/test/business"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("business rule violated"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && ./mvnw test -Dtest=GlobalExceptionHandlerTest -pl . 2>&1 | tail -20
```

Expected: FAIL — classes don't exist yet.

- [ ] **Step 3: Create EntityNotFoundException.java**

```java
package com.octane.shared.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
```

- [ ] **Step 4: Create BusinessException.java**

```java
package com.octane.shared.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
```

- [ ] **Step 5: Create ErrorResponse.java**

```java
package com.octane.shared.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String message, LocalDateTime timestamp) {}
```

- [ ] **Step 6: Create GlobalExceptionHandler.java**

```java
package com.octane.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException ex) {
        return new ErrorResponse(ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleBusiness(BusinessException ex) {
        return new ErrorResponse(ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");
        return new ErrorResponse(message, LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        return new ErrorResponse("Internal server error", LocalDateTime.now());
    }
}
```

- [ ] **Step 7: Run test to confirm it passes**

```bash
cd backend && ./mvnw test -Dtest=GlobalExceptionHandlerTest -pl . 2>&1 | tail -20
```

Expected: BUILD SUCCESS, 2 tests passed.

- [ ] **Step 8: Delete .gitkeep and commit**

```bash
rm backend/src/main/java/com/octane/shared/exception/.gitkeep
git add backend/src/main/java/com/octane/shared/exception/ \
        backend/src/test/java/com/octane/shared/exception/
git commit -m "feat(backend): add shared exceptions and global exception handler"
```

---

## Task 3: Domain Enums + JPA Entities

**Files:**
- Create: `backend/src/main/java/com/octane/station/domain/FuelUnit.java`
- Create: `backend/src/main/java/com/octane/station/domain/PumpStatus.java`
- Create: `backend/src/main/java/com/octane/station/domain/Station.java`
- Create: `backend/src/main/java/com/octane/station/domain/Fuel.java`
- Create: `backend/src/main/java/com/octane/station/domain/Pump.java`
- Create: `backend/src/main/java/com/octane/station/domain/Nozzle.java`
- Delete: `backend/src/main/java/com/octane/station/domain/.gitkeep`

- [ ] **Step 1: Create FuelUnit.java**

```java
package com.octane.station.domain;

public enum FuelUnit {
    LITER
}
```

- [ ] **Step 2: Create PumpStatus.java**

```java
package com.octane.station.domain;

public enum PumpStatus {
    ACTIVE, INACTIVE, MAINTENANCE
}
```

- [ ] **Step 3: Create Station.java**

```java
package com.octane.station.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Station() {}

    public Station(UUID id, String name, String cnpj, String address, String city,
                   String state, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.cnpj = cnpj;
        this.address = address;
        this.city = city;
        this.state = state;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCnpj() { return cnpj; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

- [ ] **Step 4: Create Fuel.java**

```java
package com.octane.station.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fuels")
public class Fuel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FuelUnit unit;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Fuel() {}

    public Fuel(UUID id, String name, FuelUnit unit, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public FuelUnit getUnit() { return unit; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 5: Create Pump.java**

```java
package com.octane.station.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pumps")
public class Pump {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PumpStatus status;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Pump() {}

    public Pump(UUID id, int number, PumpStatus status, Station station,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.number = number;
        this.status = status;
        this.station = station;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public int getNumber() { return number; }
    public PumpStatus getStatus() { return status; }
    public Station getStation() { return station; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

- [ ] **Step 6: Create Nozzle.java**

```java
package com.octane.station.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nozzles")
public class Nozzle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int number;

    @ManyToOne
    @JoinColumn(name = "pump_id", nullable = false)
    private Pump pump;

    @ManyToOne
    @JoinColumn(name = "fuel_id", nullable = false)
    private Fuel fuel;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Nozzle() {}

    public Nozzle(UUID id, int number, Pump pump, Fuel fuel, boolean active,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.number = number;
        this.pump = pump;
        this.fuel = fuel;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public int getNumber() { return number; }
    public Pump getPump() { return pump; }
    public Fuel getFuel() { return fuel; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

- [ ] **Step 7: Delete .gitkeep and verify compilation**

```bash
rm backend/src/main/java/com/octane/station/domain/.gitkeep
cd backend && ./mvnw compile -pl . 2>&1 | tail -10
```

Expected: BUILD SUCCESS.

- [ ] **Step 8: Commit**

```bash
git add backend/src/main/java/com/octane/station/domain/
git commit -m "feat(backend): add domain enums and JPA entities"
```

---

## Task 4: Domain Repository Interfaces

**Files:**
- Create: `backend/src/main/java/com/octane/station/domain/repository/StationRepository.java`
- Create: `backend/src/main/java/com/octane/station/domain/repository/FuelRepository.java`
- Create: `backend/src/main/java/com/octane/station/domain/repository/PumpRepository.java`
- Create: `backend/src/main/java/com/octane/station/domain/repository/NozzleRepository.java`

- [ ] **Step 1: Create StationRepository.java**

```java
package com.octane.station.domain.repository;

import com.octane.station.domain.Station;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StationRepository {
    Station save(Station station);
    Optional<Station> findById(UUID id);
    Optional<Station> findByCnpj(String cnpj);
    List<Station> findAll();
}
```

- [ ] **Step 2: Create FuelRepository.java**

```java
package com.octane.station.domain.repository;

import com.octane.station.domain.Fuel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelRepository {
    Optional<Fuel> findById(UUID id);
    List<Fuel> findAll();
}
```

- [ ] **Step 3: Create PumpRepository.java**

```java
package com.octane.station.domain.repository;

import com.octane.station.domain.Pump;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PumpRepository {
    Pump save(Pump pump);
    Optional<Pump> findById(UUID id);
    List<Pump> findByStationId(UUID stationId);
    boolean existsByStationIdAndNumber(UUID stationId, int number);
}
```

- [ ] **Step 4: Create NozzleRepository.java**

```java
package com.octane.station.domain.repository;

import com.octane.station.domain.Nozzle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NozzleRepository {
    Nozzle save(Nozzle nozzle);
    Optional<Nozzle> findById(UUID id);
    List<Nozzle> findByPumpId(UUID pumpId);
    boolean existsByPumpIdAndNumber(UUID pumpId, int number);
}
```

- [ ] **Step 5: Verify compilation and commit**

```bash
cd backend && ./mvnw compile -pl . 2>&1 | tail -5
git add backend/src/main/java/com/octane/station/domain/repository/
git commit -m "feat(backend): add domain repository interfaces"
```

---

## Task 5: Infrastructure — JPA Repositories and Implementations

**Files:**
- Create: `backend/src/main/java/com/octane/station/repository/StationJpaRepository.java`
- Create: `backend/src/main/java/com/octane/station/repository/StationRepositoryImpl.java`
- Create: `backend/src/main/java/com/octane/station/repository/FuelJpaRepository.java`
- Create: `backend/src/main/java/com/octane/station/repository/FuelRepositoryImpl.java`
- Create: `backend/src/main/java/com/octane/station/repository/PumpJpaRepository.java`
- Create: `backend/src/main/java/com/octane/station/repository/PumpRepositoryImpl.java`
- Create: `backend/src/main/java/com/octane/station/repository/NozzleJpaRepository.java`
- Create: `backend/src/main/java/com/octane/station/repository/NozzleRepositoryImpl.java`
- Delete: `backend/src/main/java/com/octane/station/repository/.gitkeep`

- [ ] **Step 1: Create StationJpaRepository.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface StationJpaRepository extends JpaRepository<Station, UUID> {
    Optional<Station> findByCnpj(String cnpj);
}
```

- [ ] **Step 2: Create StationRepositoryImpl.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StationRepositoryImpl implements StationRepository {

    private final StationJpaRepository jpaRepository;

    public StationRepositoryImpl(StationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Station save(Station station) {
        return jpaRepository.save(station);
    }

    @Override
    public Optional<Station> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Station> findByCnpj(String cnpj) {
        return jpaRepository.findByCnpj(cnpj);
    }

    @Override
    public List<Station> findAll() {
        return jpaRepository.findAll();
    }
}
```

- [ ] **Step 3: Create FuelJpaRepository.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Fuel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface FuelJpaRepository extends JpaRepository<Fuel, UUID> {}
```

- [ ] **Step 4: Create FuelRepositoryImpl.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FuelRepositoryImpl implements FuelRepository {

    private final FuelJpaRepository jpaRepository;

    public FuelRepositoryImpl(FuelJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Fuel> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Fuel> findAll() {
        return jpaRepository.findAll();
    }
}
```

- [ ] **Step 5: Create PumpJpaRepository.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Pump;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface PumpJpaRepository extends JpaRepository<Pump, UUID> {
    List<Pump> findByStation_Id(UUID stationId);
    boolean existsByStation_IdAndNumber(UUID stationId, int number);
}
```

- [ ] **Step 6: Create PumpRepositoryImpl.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Pump;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PumpRepositoryImpl implements PumpRepository {

    private final PumpJpaRepository jpaRepository;

    public PumpRepositoryImpl(PumpJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pump save(Pump pump) {
        return jpaRepository.save(pump);
    }

    @Override
    public Optional<Pump> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Pump> findByStationId(UUID stationId) {
        return jpaRepository.findByStation_Id(stationId);
    }

    @Override
    public boolean existsByStationIdAndNumber(UUID stationId, int number) {
        return jpaRepository.existsByStation_IdAndNumber(stationId, number);
    }
}
```

- [ ] **Step 7: Create NozzleJpaRepository.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Nozzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface NozzleJpaRepository extends JpaRepository<Nozzle, UUID> {
    List<Nozzle> findByPump_Id(UUID pumpId);
    boolean existsByPump_IdAndNumber(UUID pumpId, int number);
}
```

- [ ] **Step 8: Create NozzleRepositoryImpl.java**

```java
package com.octane.station.repository;

import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NozzleRepositoryImpl implements NozzleRepository {

    private final NozzleJpaRepository jpaRepository;

    public NozzleRepositoryImpl(NozzleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Nozzle save(Nozzle nozzle) {
        return jpaRepository.save(nozzle);
    }

    @Override
    public Optional<Nozzle> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Nozzle> findByPumpId(UUID pumpId) {
        return jpaRepository.findByPump_Id(pumpId);
    }

    @Override
    public boolean existsByPumpIdAndNumber(UUID pumpId, int number) {
        return jpaRepository.existsByPump_IdAndNumber(pumpId, number);
    }
}
```

- [ ] **Step 9: Delete .gitkeep, verify compilation and commit**

```bash
rm backend/src/main/java/com/octane/station/repository/.gitkeep
cd backend && ./mvnw compile -pl . 2>&1 | tail -5
git add backend/src/main/java/com/octane/station/repository/
git commit -m "feat(backend): add JPA repositories and infrastructure implementations"
```

---

## Task 6: Station Use Cases (TDD)

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/station/CreateStationRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/station/CreateStationUseCase.java`
- Create: `backend/src/main/java/com/octane/station/usecase/station/FindStationUseCase.java`
- Create: `backend/src/main/java/com/octane/station/usecase/station/ListStationsUseCase.java`
- Create: `backend/src/test/java/com/octane/station/usecase/station/CreateStationUseCaseTest.java`
- Create: `backend/src/test/java/com/octane/station/usecase/station/FindStationUseCaseTest.java`
- Delete: `backend/src/main/java/com/octane/station/usecase/.gitkeep`

- [ ] **Step 1: Create CreateStationRequest.java**

```java
package com.octane.station.usecase.station;

public record CreateStationRequest(
    String name,
    String cnpj,
    String address,
    String city,
    String state
) {}
```

- [ ] **Step 2: Write failing tests for CreateStationUseCase**

Create `backend/src/test/java/com/octane/station/usecase/station/CreateStationUseCaseTest.java`:

```java
package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
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
class CreateStationUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private CreateStationUseCase sut;

    @Test
    void execute_savesAndReturnsStation_whenCnpjIsNew() {
        var request = new CreateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP");
        var saved = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.empty());
        when(stationRepository.save(any(Station.class))).thenReturn(saved);

        var result = sut.execute(request);

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getCnpj()).isEqualTo("12.345.678/0001-90");
        verify(stationRepository).save(any(Station.class));
    }

    @Test
    void execute_throwsBusinessException_whenCnpjAlreadyExists() {
        var request = new CreateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP");
        var existing = new Station(UUID.randomUUID(), "Posto Y", "12.345.678/0001-90", "Rua B, 2",
            "Rio de Janeiro", "RJ", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CNPJ");

        verify(stationRepository, never()).save(any());
    }
}
```

- [ ] **Step 3: Run tests to confirm they fail**

```bash
cd backend && ./mvnw test -Dtest=CreateStationUseCaseTest -pl . 2>&1 | tail -10
```

Expected: FAIL — `CreateStationUseCase` not found.

- [ ] **Step 4: Create CreateStationUseCase.java**

```java
package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateStationUseCase {

    private final StationRepository stationRepository;

    public CreateStationUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Station execute(CreateStationRequest request) {
        if (stationRepository.findByCnpj(request.cnpj()).isPresent()) {
            throw new BusinessException("CNPJ já cadastrado: " + request.cnpj());
        }
        var now = LocalDateTime.now();
        var station = new Station(null, request.name(), request.cnpj(),
            request.address(), request.city(), request.state(), true, now, now);
        return stationRepository.save(station);
    }
}
```

- [ ] **Step 5: Run tests to confirm they pass**

```bash
cd backend && ./mvnw test -Dtest=CreateStationUseCaseTest -pl . 2>&1 | tail -10
```

Expected: BUILD SUCCESS, 2 tests passed.

- [ ] **Step 6: Write failing tests for FindStationUseCase**

Create `backend/src/test/java/com/octane/station/usecase/station/FindStationUseCaseTest.java`:

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindStationUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private FindStationUseCase sut;

    @Test
    void execute_returnsStation_whenFound() {
        var id = UUID.randomUUID();
        var station = new Station(id, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(id)).thenReturn(Optional.of(station));

        var result = sut.execute(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void execute_throwsEntityNotFoundException_whenNotFound() {
        var id = UUID.randomUUID();
        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(id.toString());
    }
}
```

- [ ] **Step 7: Run test to confirm it fails**

```bash
cd backend && ./mvnw test -Dtest=FindStationUseCaseTest -pl . 2>&1 | tail -10
```

Expected: FAIL — `FindStationUseCase` not found.

- [ ] **Step 8: Create FindStationUseCase.java**

```java
package com.octane.station.usecase.station;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindStationUseCase {

    private final StationRepository stationRepository;

    public FindStationUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Station execute(UUID id) {
        return stationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + id));
    }
}
```

- [ ] **Step 9: Create ListStationsUseCase.java** (no dedicated test — simple delegation)

```java
package com.octane.station.usecase.station;

import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListStationsUseCase {

    private final StationRepository stationRepository;

    public ListStationsUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> execute() {
        return stationRepository.findAll();
    }
}
```

- [ ] **Step 10: Run all station use case tests**

```bash
cd backend && ./mvnw test -Dtest="CreateStationUseCaseTest,FindStationUseCaseTest" -pl . 2>&1 | tail -10
```

Expected: BUILD SUCCESS, 4 tests passed.

- [ ] **Step 11: Delete .gitkeep and commit**

```bash
rm backend/src/main/java/com/octane/station/usecase/.gitkeep
git add backend/src/main/java/com/octane/station/usecase/station/ \
        backend/src/test/java/com/octane/station/usecase/station/
git commit -m "feat(backend): add station use cases with tests"
```

---

## Task 7: Pump Use Cases (TDD)

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/pump/CreatePumpRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/pump/CreatePumpUseCase.java`
- Create: `backend/src/main/java/com/octane/station/usecase/pump/ListPumpsByStationUseCase.java`
- Create: `backend/src/test/java/com/octane/station/usecase/pump/CreatePumpUseCaseTest.java`

- [ ] **Step 1: Create CreatePumpRequest.java**

```java
package com.octane.station.usecase.pump;

public record CreatePumpRequest(int number) {}
```

- [ ] **Step 2: Write failing test for CreatePumpUseCase**

Create `backend/src/test/java/com/octane/station/usecase/pump/CreatePumpUseCaseTest.java`:

```java
package com.octane.station.usecase.pump;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.PumpRepository;
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
class CreatePumpUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private PumpRepository pumpRepository;

    @InjectMocks
    private CreatePumpUseCase sut;

    @Test
    void execute_savesAndReturnsPump_whenNumberIsAvailable() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var request = new CreatePumpRequest(1);
        var saved = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
            LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pumpRepository.existsByStationIdAndNumber(stationId, 1)).thenReturn(false);
        when(pumpRepository.save(any(Pump.class))).thenReturn(saved);

        var result = sut.execute(stationId, request);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(PumpStatus.ACTIVE);
        verify(pumpRepository).save(any(Pump.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenStationNotFound() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId, new CreatePumpRequest(1)))
            .isInstanceOf(EntityNotFoundException.class);

        verify(pumpRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenNumberAlreadyUsed() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pumpRepository.existsByStationIdAndNumber(stationId, 1)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(stationId, new CreatePumpRequest(1)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("1");

        verify(pumpRepository, never()).save(any());
    }
}
```

- [ ] **Step 3: Run test to confirm it fails**

```bash
cd backend && ./mvnw test -Dtest=CreatePumpUseCaseTest -pl . 2>&1 | tail -10
```

Expected: FAIL — `CreatePumpUseCase` not found.

- [ ] **Step 4: Create CreatePumpUseCase.java**

```java
package com.octane.station.usecase.pump;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.PumpRepository;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreatePumpUseCase {

    private final StationRepository stationRepository;
    private final PumpRepository pumpRepository;

    public CreatePumpUseCase(StationRepository stationRepository, PumpRepository pumpRepository) {
        this.stationRepository = stationRepository;
        this.pumpRepository = pumpRepository;
    }

    @Transactional
    public Pump execute(UUID stationId, CreatePumpRequest request) {
        var station = stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));

        if (pumpRepository.existsByStationIdAndNumber(stationId, request.number())) {
            throw new BusinessException("Bomba número " + request.number() + " já existe neste posto");
        }

        var now = LocalDateTime.now();
        var pump = new Pump(null, request.number(), PumpStatus.ACTIVE, station, now, now);
        return pumpRepository.save(pump);
    }
}
```

- [ ] **Step 5: Create ListPumpsByStationUseCase.java**

```java
package com.octane.station.usecase.pump;

import com.octane.station.domain.Pump;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListPumpsByStationUseCase {

    private final PumpRepository pumpRepository;

    public ListPumpsByStationUseCase(PumpRepository pumpRepository) {
        this.pumpRepository = pumpRepository;
    }

    public List<Pump> execute(UUID stationId) {
        return pumpRepository.findByStationId(stationId);
    }
}
```

- [ ] **Step 6: Run tests to confirm they pass**

```bash
cd backend && ./mvnw test -Dtest=CreatePumpUseCaseTest -pl . 2>&1 | tail -10
```

Expected: BUILD SUCCESS, 3 tests passed.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/octane/station/usecase/pump/ \
        backend/src/test/java/com/octane/station/usecase/pump/
git commit -m "feat(backend): add pump use cases with tests"
```

---

## Task 8: Nozzle Use Cases (TDD)

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/nozzle/CreateNozzleRequest.java`
- Create: `backend/src/main/java/com/octane/station/usecase/nozzle/CreateNozzleUseCase.java`
- Create: `backend/src/main/java/com/octane/station/usecase/nozzle/ListNozzlesByPumpUseCase.java`
- Create: `backend/src/test/java/com/octane/station/usecase/nozzle/CreateNozzleUseCaseTest.java`

- [ ] **Step 1: Create CreateNozzleRequest.java**

```java
package com.octane.station.usecase.nozzle;

import java.util.UUID;

public record CreateNozzleRequest(int number, UUID fuelId) {}
```

- [ ] **Step 2: Write failing test for CreateNozzleUseCase**

Create `backend/src/test/java/com/octane/station/usecase/nozzle/CreateNozzleUseCaseTest.java`:

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
class CreateNozzleUseCaseTest {

    @Mock
    private PumpRepository pumpRepository;

    @Mock
    private FuelRepository fuelRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private CreateNozzleUseCase sut;

    private Station buildStation() {
        return new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "SP", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Pump buildPump(UUID pumpId) {
        return new Pump(pumpId, 1, PumpStatus.ACTIVE, buildStation(),
            LocalDateTime.now(), LocalDateTime.now());
    }

    private Fuel buildFuel(UUID fuelId) {
        return new Fuel(fuelId, "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
    }

    @Test
    void execute_savesNozzle_whenInputsAreValid() {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        var request = new CreateNozzleRequest(1, fuelId);
        var pump = buildPump(pumpId);
        var fuel = buildFuel(fuelId);
        var saved = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.of(fuel));
        when(nozzleRepository.existsByPumpIdAndNumber(pumpId, 1)).thenReturn(false);
        when(nozzleRepository.save(any(Nozzle.class))).thenReturn(saved);

        var result = sut.execute(pumpId, request);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFuel().getId()).isEqualTo(fuelId);
        verify(nozzleRepository).save(any(Nozzle.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenPumpNotFound() {
        var pumpId = UUID.randomUUID();
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(pumpId, new CreateNozzleRequest(1, UUID.randomUUID())))
            .isInstanceOf(EntityNotFoundException.class);

        verify(nozzleRepository, never()).save(any());
    }

    @Test
    void execute_throwsEntityNotFoundException_whenFuelNotFound() {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(buildPump(pumpId)));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(pumpId, new CreateNozzleRequest(1, fuelId)))
            .isInstanceOf(EntityNotFoundException.class);

        verify(nozzleRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenNozzleNumberAlreadyUsed() {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(buildPump(pumpId)));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.of(buildFuel(fuelId)));
        when(nozzleRepository.existsByPumpIdAndNumber(pumpId, 1)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(pumpId, new CreateNozzleRequest(1, fuelId)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("1");

        verify(nozzleRepository, never()).save(any());
    }
}
```

- [ ] **Step 3: Run test to confirm it fails**

```bash
cd backend && ./mvnw test -Dtest=CreateNozzleUseCaseTest -pl . 2>&1 | tail -10
```

Expected: FAIL — `CreateNozzleUseCase` not found.

- [ ] **Step 4: Create CreateNozzleUseCase.java**

```java
package com.octane.station.usecase.nozzle;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.NozzleRepository;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateNozzleUseCase {

    private final PumpRepository pumpRepository;
    private final FuelRepository fuelRepository;
    private final NozzleRepository nozzleRepository;

    public CreateNozzleUseCase(PumpRepository pumpRepository, FuelRepository fuelRepository,
                               NozzleRepository nozzleRepository) {
        this.pumpRepository = pumpRepository;
        this.fuelRepository = fuelRepository;
        this.nozzleRepository = nozzleRepository;
    }

    @Transactional
    public Nozzle execute(UUID pumpId, CreateNozzleRequest request) {
        var pump = pumpRepository.findById(pumpId)
            .orElseThrow(() -> new EntityNotFoundException("Pump not found: " + pumpId));

        var fuel = fuelRepository.findById(request.fuelId())
            .orElseThrow(() -> new EntityNotFoundException("Fuel not found: " + request.fuelId()));

        if (nozzleRepository.existsByPumpIdAndNumber(pumpId, request.number())) {
            throw new BusinessException("Bico número " + request.number() + " já existe nesta bomba");
        }

        var now = LocalDateTime.now();
        var nozzle = new Nozzle(null, request.number(), pump, fuel, true, now, now);
        return nozzleRepository.save(nozzle);
    }
}
```

- [ ] **Step 5: Create ListNozzlesByPumpUseCase.java**

```java
package com.octane.station.usecase.nozzle;

import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListNozzlesByPumpUseCase {

    private final NozzleRepository nozzleRepository;

    public ListNozzlesByPumpUseCase(NozzleRepository nozzleRepository) {
        this.nozzleRepository = nozzleRepository;
    }

    public List<Nozzle> execute(UUID pumpId) {
        return nozzleRepository.findByPumpId(pumpId);
    }
}
```

- [ ] **Step 6: Run tests to confirm they pass**

```bash
cd backend && ./mvnw test -Dtest=CreateNozzleUseCaseTest -pl . 2>&1 | tail -10
```

Expected: BUILD SUCCESS, 4 tests passed.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/octane/station/usecase/nozzle/ \
        backend/src/test/java/com/octane/station/usecase/nozzle/
git commit -m "feat(backend): add nozzle use cases with tests"
```

---

## Task 9: Fuel Use Case

**Files:**
- Create: `backend/src/main/java/com/octane/station/usecase/fuel/ListFuelsUseCase.java`

- [ ] **Step 1: Create ListFuelsUseCase.java**

```java
package com.octane.station.usecase.fuel;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListFuelsUseCase {

    private final FuelRepository fuelRepository;

    public ListFuelsUseCase(FuelRepository fuelRepository) {
        this.fuelRepository = fuelRepository;
    }

    public List<Fuel> execute() {
        return fuelRepository.findAll();
    }
}
```

- [ ] **Step 2: Verify compilation and commit**

```bash
cd backend && ./mvnw compile -pl . 2>&1 | tail -5
git add backend/src/main/java/com/octane/station/usecase/fuel/
git commit -m "feat(backend): add fuel use case"
```

---

## Task 10: Response DTOs

**Files:**
- Create: `backend/src/main/java/com/octane/station/handler/StationResponse.java`
- Create: `backend/src/main/java/com/octane/station/handler/PumpResponse.java`
- Create: `backend/src/main/java/com/octane/station/handler/NozzleResponse.java`
- Create: `backend/src/main/java/com/octane/station/handler/FuelResponse.java`

- [ ] **Step 1: Create StationResponse.java**

```java
package com.octane.station.handler;

import com.octane.station.domain.Station;

import java.util.UUID;

public record StationResponse(
    UUID id,
    String name,
    String cnpj,
    String address,
    String city,
    String state,
    boolean active
) {
    public static StationResponse from(Station station) {
        return new StationResponse(
            station.getId(),
            station.getName(),
            station.getCnpj(),
            station.getAddress(),
            station.getCity(),
            station.getState(),
            station.isActive()
        );
    }
}
```

- [ ] **Step 2: Create PumpResponse.java**

```java
package com.octane.station.handler;

import com.octane.station.domain.Pump;

import java.util.UUID;

public record PumpResponse(
    UUID id,
    int number,
    String status,
    UUID stationId
) {
    public static PumpResponse from(Pump pump) {
        return new PumpResponse(
            pump.getId(),
            pump.getNumber(),
            pump.getStatus().name(),
            pump.getStation().getId()
        );
    }
}
```

- [ ] **Step 3: Create NozzleResponse.java**

```java
package com.octane.station.handler;

import com.octane.station.domain.Nozzle;

import java.util.UUID;

public record NozzleResponse(
    UUID id,
    int number,
    UUID pumpId,
    UUID fuelId,
    boolean active
) {
    public static NozzleResponse from(Nozzle nozzle) {
        return new NozzleResponse(
            nozzle.getId(),
            nozzle.getNumber(),
            nozzle.getPump().getId(),
            nozzle.getFuel().getId(),
            nozzle.isActive()
        );
    }
}
```

- [ ] **Step 4: Create FuelResponse.java**

```java
package com.octane.station.handler;

import com.octane.station.domain.Fuel;

import java.util.UUID;

public record FuelResponse(
    UUID id,
    String name,
    String unit,
    boolean active
) {
    public static FuelResponse from(Fuel fuel) {
        return new FuelResponse(
            fuel.getId(),
            fuel.getName(),
            fuel.getUnit().name(),
            fuel.isActive()
        );
    }
}
```

- [ ] **Step 5: Verify compilation**

```bash
cd backend && ./mvnw compile -pl . 2>&1 | tail -5
```

Expected: BUILD SUCCESS.

---

## Task 11: StationHandler (TDD)

**Files:**
- Create: `backend/src/main/java/com/octane/station/handler/StationHandler.java`
- Create: `backend/src/test/java/com/octane/station/handler/StationHandlerTest.java`

- [ ] **Step 1: Write failing test for StationHandler**

Create `backend/src/test/java/com/octane/station/handler/StationHandlerTest.java`:

```java
package com.octane.station.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.usecase.pump.CreatePumpRequest;
import com.octane.station.usecase.pump.CreatePumpUseCase;
import com.octane.station.usecase.pump.ListPumpsByStationUseCase;
import com.octane.station.usecase.station.CreateStationRequest;
import com.octane.station.usecase.station.CreateStationUseCase;
import com.octane.station.usecase.station.FindStationUseCase;
import com.octane.station.usecase.station.ListStationsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(StationHandler.class)
class StationHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateStationUseCase createStationUseCase;

    @MockitoBean
    private FindStationUseCase findStationUseCase;

    @MockitoBean
    private ListStationsUseCase listStationsUseCase;

    @MockitoBean
    private CreatePumpUseCase createPumpUseCase;

    @MockitoBean
    private ListPumpsByStationUseCase listPumpsByStationUseCase;

    private Station buildStation(UUID id) {
        return new Station(id, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Pump buildPump(UUID id, Station station) {
        return new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void postStations_returns201WithBody() throws Exception {
        var id = UUID.randomUUID();
        var station = buildStation(id);
        when(createStationUseCase.execute(any(CreateStationRequest.class))).thenReturn(station);

        mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"));
    }

    @Test
    void getStations_returns200WithList() throws Exception {
        var station = buildStation(UUID.randomUUID());
        when(listStationsUseCase.execute()).thenReturn(List.of(station));

        mockMvc.perform(get("/api/stations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Posto X"));
    }

    @Test
    void getStationById_returns200_whenFound() throws Exception {
        var id = UUID.randomUUID();
        when(findStationUseCase.execute(id)).thenReturn(buildStation(id));

        mockMvc.perform(get("/api/stations/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getStationById_returns404_whenNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(findStationUseCase.execute(id)).thenThrow(new EntityNotFoundException("Station not found: " + id));

        mockMvc.perform(get("/api/stations/" + id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Station not found: " + id));
    }

    @Test
    void postPumps_returns201WithBody() throws Exception {
        var stationId = UUID.randomUUID();
        var station = buildStation(stationId);
        var pump = buildPump(UUID.randomUUID(), station);
        when(createPumpUseCase.execute(eq(stationId), any(CreatePumpRequest.class))).thenReturn(pump);

        mockMvc.perform(post("/api/stations/" + stationId + "/pumps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreatePumpRequest(1))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getPumps_returns200WithList() throws Exception {
        var stationId = UUID.randomUUID();
        var station = buildStation(stationId);
        when(listPumpsByStationUseCase.execute(stationId)).thenReturn(List.of(buildPump(UUID.randomUUID(), station)));

        mockMvc.perform(get("/api/stations/" + stationId + "/pumps"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].number").value(1));
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && ./mvnw test -Dtest=StationHandlerTest -pl . 2>&1 | tail -10
```

Expected: FAIL — `StationHandler` not found.

- [ ] **Step 3: Create StationHandler.java**

```java
package com.octane.station.handler;

import com.octane.station.usecase.pump.CreatePumpRequest;
import com.octane.station.usecase.pump.CreatePumpUseCase;
import com.octane.station.usecase.pump.ListPumpsByStationUseCase;
import com.octane.station.usecase.station.CreateStationRequest;
import com.octane.station.usecase.station.CreateStationUseCase;
import com.octane.station.usecase.station.FindStationUseCase;
import com.octane.station.usecase.station.ListStationsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stations")
public class StationHandler {

    private final CreateStationUseCase createStationUseCase;
    private final FindStationUseCase findStationUseCase;
    private final ListStationsUseCase listStationsUseCase;
    private final CreatePumpUseCase createPumpUseCase;
    private final ListPumpsByStationUseCase listPumpsByStationUseCase;

    public StationHandler(
        CreateStationUseCase createStationUseCase,
        FindStationUseCase findStationUseCase,
        ListStationsUseCase listStationsUseCase,
        CreatePumpUseCase createPumpUseCase,
        ListPumpsByStationUseCase listPumpsByStationUseCase
    ) {
        this.createStationUseCase = createStationUseCase;
        this.findStationUseCase = findStationUseCase;
        this.listStationsUseCase = listStationsUseCase;
        this.createPumpUseCase = createPumpUseCase;
        this.listPumpsByStationUseCase = listPumpsByStationUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse create(@RequestBody CreateStationRequest request) {
        return StationResponse.from(createStationUseCase.execute(request));
    }

    @GetMapping
    public List<StationResponse> list() {
        return listStationsUseCase.execute().stream()
            .map(StationResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public StationResponse findById(@PathVariable UUID id) {
        return StationResponse.from(findStationUseCase.execute(id));
    }

    @PostMapping("/{id}/pumps")
    @ResponseStatus(HttpStatus.CREATED)
    public PumpResponse createPump(@PathVariable UUID id, @RequestBody CreatePumpRequest request) {
        return PumpResponse.from(createPumpUseCase.execute(id, request));
    }

    @GetMapping("/{id}/pumps")
    public List<PumpResponse> listPumps(@PathVariable UUID id) {
        return listPumpsByStationUseCase.execute(id).stream()
            .map(PumpResponse::from)
            .toList();
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
cd backend && ./mvnw test -Dtest=StationHandlerTest -pl . 2>&1 | tail -10
```

Expected: BUILD SUCCESS, 6 tests passed.

---

## Task 12: PumpHandler, NozzleHandler, FuelHandler (TDD)

**Files:**
- Create: `backend/src/main/java/com/octane/station/handler/PumpHandler.java`
- Create: `backend/src/main/java/com/octane/station/handler/NozzleHandler.java`
- Create: `backend/src/main/java/com/octane/station/handler/FuelHandler.java`
- Create: `backend/src/test/java/com/octane/station/handler/PumpHandlerTest.java`
- Create: `backend/src/test/java/com/octane/station/handler/FuelHandlerTest.java`

- [ ] **Step 1: Write failing test for PumpHandler (nozzle sub-resource)**

Create `backend/src/test/java/com/octane/station/handler/PumpHandlerTest.java`:

```java
package com.octane.station.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.usecase.nozzle.CreateNozzleRequest;
import com.octane.station.usecase.nozzle.CreateNozzleUseCase;
import com.octane.station.usecase.nozzle.ListNozzlesByPumpUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(PumpHandler.class)
class PumpHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateNozzleUseCase createNozzleUseCase;

    @MockitoBean
    private ListNozzlesByPumpUseCase listNozzlesByPumpUseCase;

    private Nozzle buildNozzle(UUID pumpId, UUID fuelId) {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(pumpId, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(fuelId, "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        return new Nozzle(UUID.randomUUID(), 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void postNozzles_returns201WithBody() throws Exception {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        var nozzle = buildNozzle(pumpId, fuelId);
        when(createNozzleUseCase.execute(eq(pumpId), any(CreateNozzleRequest.class))).thenReturn(nozzle);

        mockMvc.perform(post("/api/pumps/" + pumpId + "/nozzles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateNozzleRequest(1, fuelId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getNozzles_returns200WithList() throws Exception {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(listNozzlesByPumpUseCase.execute(pumpId)).thenReturn(List.of(buildNozzle(pumpId, fuelId)));

        mockMvc.perform(get("/api/pumps/" + pumpId + "/nozzles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].number").value(1));
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && ./mvnw test -Dtest=PumpHandlerTest -pl . 2>&1 | tail -10
```

Expected: FAIL — `PumpHandler` not found.

- [ ] **Step 3: Create PumpHandler.java**

```java
package com.octane.station.handler;

import com.octane.station.usecase.nozzle.CreateNozzleRequest;
import com.octane.station.usecase.nozzle.CreateNozzleUseCase;
import com.octane.station.usecase.nozzle.ListNozzlesByPumpUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pumps")
public class PumpHandler {

    private final CreateNozzleUseCase createNozzleUseCase;
    private final ListNozzlesByPumpUseCase listNozzlesByPumpUseCase;

    public PumpHandler(CreateNozzleUseCase createNozzleUseCase,
                       ListNozzlesByPumpUseCase listNozzlesByPumpUseCase) {
        this.createNozzleUseCase = createNozzleUseCase;
        this.listNozzlesByPumpUseCase = listNozzlesByPumpUseCase;
    }

    @PostMapping("/{id}/nozzles")
    @ResponseStatus(HttpStatus.CREATED)
    public NozzleResponse createNozzle(@PathVariable UUID id, @RequestBody CreateNozzleRequest request) {
        return NozzleResponse.from(createNozzleUseCase.execute(id, request));
    }

    @GetMapping("/{id}/nozzles")
    public List<NozzleResponse> listNozzles(@PathVariable UUID id) {
        return listNozzlesByPumpUseCase.execute(id).stream()
            .map(NozzleResponse::from)
            .toList();
    }
}
```

- [ ] **Step 4: Create NozzleHandler.java** (empty skeleton — no endpoints at this scope)

```java
package com.octane.station.handler;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nozzles")
public class NozzleHandler {}
```

- [ ] **Step 5: Write failing test for FuelHandler**

Create `backend/src/test/java/com/octane/station/handler/FuelHandlerTest.java`:

```java
package com.octane.station.handler;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.usecase.fuel.ListFuelsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FuelHandler.class)
class FuelHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListFuelsUseCase listFuelsUseCase;

    @Test
    void getFuels_returns200WithList() throws Exception {
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        when(listFuelsUseCase.execute()).thenReturn(List.of(fuel));

        mockMvc.perform(get("/api/fuels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Gasolina Comum"))
            .andExpect(jsonPath("$[0].unit").value("LITER"));
    }
}
```

- [ ] **Step 6: Run test to confirm it fails**

```bash
cd backend && ./mvnw test -Dtest=FuelHandlerTest -pl . 2>&1 | tail -10
```

Expected: FAIL — `FuelHandler` not found.

- [ ] **Step 7: Create FuelHandler.java**

```java
package com.octane.station.handler;

import com.octane.station.usecase.fuel.ListFuelsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fuels")
public class FuelHandler {

    private final ListFuelsUseCase listFuelsUseCase;

    public FuelHandler(ListFuelsUseCase listFuelsUseCase) {
        this.listFuelsUseCase = listFuelsUseCase;
    }

    @GetMapping
    public List<FuelResponse> list() {
        return listFuelsUseCase.execute().stream()
            .map(FuelResponse::from)
            .toList();
    }
}
```

- [ ] **Step 8: Run all handler tests**

```bash
cd backend && ./mvnw test -Dtest="PumpHandlerTest,FuelHandlerTest" -pl . 2>&1 | tail -10
```

Expected: BUILD SUCCESS, 3 tests passed.

- [ ] **Step 9: Run the full test suite**

```bash
cd backend && ./mvnw test -pl . 2>&1 | tail -15
```

Expected: BUILD SUCCESS — all tests pass (HealthHandlerTest + GlobalExceptionHandlerTest + use case tests + handler tests).

- [ ] **Step 10: Delete .gitkeep, commit everything**

```bash
rm -f backend/src/main/java/com/octane/station/handler/.gitkeep
git add backend/src/main/java/com/octane/station/handler/ \
        backend/src/test/java/com/octane/station/handler/
git commit -m "feat(backend): add REST handlers for stations, pumps, nozzles and fuels"
```

---

## Self-Review

**Spec coverage:**
- Migrations V1–V4: ✅ Task 1
- Entities Station, Fuel, Pump, Nozzle (no Lombok, getters + constructors, @PreUpdate): ✅ Task 3
- Domain repository interfaces (no JpaRepository extension): ✅ Task 4
- Infrastructure impls with inner JpaRepository pattern: ✅ Task 5
- CreateStation (CNPJ duplicate check): ✅ Task 6
- FindStation (EntityNotFoundException): ✅ Task 6
- ListStations: ✅ Task 6
- CreatePump (duplicate number check, station lookup): ✅ Task 7
- ListPumpsByStation: ✅ Task 7
- CreateNozzle (duplicate check, pump + fuel lookup): ✅ Task 8
- ListNozzlesByPump: ✅ Task 8
- ListFuels: ✅ Task 9
- EntityNotFoundException → 404, BusinessException → 422: ✅ Task 2
- MethodArgumentNotValidException → 400: ✅ Task 2
- JSON error response shape: ✅ Task 2
- All 8 REST endpoints: ✅ Tasks 11–12
- Records for all DTOs: ✅ Tasks 6–10
- @Transactional on write use cases: ✅ CreateStationUseCase, CreatePumpUseCase, CreateNozzleUseCase
- No @Autowired, always private final + constructor: ✅ all files
- No Lombok: ✅ all files

**Placeholder scan:** No TBDs or TODOs found.

**Type consistency:**
- `CreateStationRequest` used in CreateStationUseCase and StationHandlerTest — same record ✅
- `CreatePumpRequest` used in CreatePumpUseCase and StationHandlerTest — same record ✅
- `CreateNozzleRequest` used in CreateNozzleUseCase and PumpHandlerTest — same record ✅
- `StationResponse.from(Station)`, `PumpResponse.from(Pump)`, etc. — factory methods called consistently ✅
- `PumpRepository.existsByStationIdAndNumber` matched in PumpRepositoryImpl delegating to `PumpJpaRepository.existsByStation_IdAndNumber` ✅
- `NozzleRepository.existsByPumpIdAndNumber` matched in NozzleRepositoryImpl delegating to `NozzleJpaRepository.existsByPump_IdAndNumber` ✅
