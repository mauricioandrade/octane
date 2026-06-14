package com.octane.station.handler;

import com.octane.station.usecase.nozzle.UpdateNozzleRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusUseCase;
import com.octane.station.usecase.nozzle.UpdateNozzleUseCase;
import jakarta.validation.Valid;
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
    public NozzleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateNozzleRequest request) {
        return NozzleResponse.from(updateNozzleUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/status")
    public NozzleResponse updateStatus(@PathVariable UUID id, @RequestBody UpdateNozzleStatusRequest request) {
        return NozzleResponse.from(updateNozzleStatusUseCase.execute(id, request));
    }
}
