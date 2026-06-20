package com.octane.inventory.handler;

import com.octane.inventory.domain.repository.TankRepository;
import com.octane.inventory.usecase.*;
import com.octane.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tanks")
public class TankHandler {

    private final CreateTankUseCase createTankUseCase;
    private final RegisterDeliveryUseCase registerDeliveryUseCase;
    private final AdjustTankLevelUseCase adjustTankLevelUseCase;
    private final TankRepository tankRepository;

    public TankHandler(CreateTankUseCase createTankUseCase,
                       RegisterDeliveryUseCase registerDeliveryUseCase,
                       AdjustTankLevelUseCase adjustTankLevelUseCase,
                       TankRepository tankRepository) {
        this.createTankUseCase = createTankUseCase;
        this.registerDeliveryUseCase = registerDeliveryUseCase;
        this.adjustTankLevelUseCase = adjustTankLevelUseCase;
        this.tankRepository = tankRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TankResponse create(@Valid @RequestBody CreateTankUseCase.Request request) {
        return createTankUseCase.execute(request);
    }

    @GetMapping
    public List<TankResponse> listByStation(@RequestParam UUID stationId) {
        return tankRepository.findByStationId(stationId).stream()
            .map(TankResponse::from)
            .toList();
    }

    @PostMapping("/{id}/deliveries")
    @ResponseStatus(HttpStatus.CREATED)
    public TankResponse registerDelivery(@PathVariable UUID id,
                                          @Valid @RequestBody RegisterDeliveryUseCase.Request request) {
        return registerDeliveryUseCase.execute(id, request);
    }

    @PostMapping("/{id}/adjustments")
    public TankResponse adjustLevel(@PathVariable UUID id,
                                     @Valid @RequestBody AdjustTankLevelUseCase.Request request) {
        return adjustTankLevelUseCase.execute(id, request);
    }

    @GetMapping("/{id}/movements")
    public PageResponse<TankMovementResponse> getMovements(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var springPage = tankRepository.findMovementsByTankId(id, PageRequest.of(page, size));
        return PageResponse.of(
            springPage.getContent().stream().map(TankMovementResponse::from).toList(),
            page, size, springPage.getTotalElements()
        );
    }
}
