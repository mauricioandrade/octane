package com.octane.station.handler;

import com.octane.station.usecase.nozzle.NozzleResponse;
import com.octane.station.usecase.nozzle.CreateNozzleRequest;
import com.octane.station.usecase.nozzle.CreateNozzleUseCase;
import com.octane.station.usecase.nozzle.ListNozzlesByPumpUseCase;
import com.octane.station.usecase.pump.PumpResponse;
import com.octane.station.usecase.pump.UpdatePumpRequest;
import com.octane.station.usecase.pump.UpdatePumpStatusRequest;
import com.octane.station.usecase.pump.UpdatePumpStatusUseCase;
import com.octane.station.usecase.pump.UpdatePumpUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pumps")
public class PumpHandler {

    private final CreateNozzleUseCase createNozzleUseCase;
    private final ListNozzlesByPumpUseCase listNozzlesByPumpUseCase;
    private final UpdatePumpUseCase updatePumpUseCase;
    private final UpdatePumpStatusUseCase updatePumpStatusUseCase;

    public PumpHandler(CreateNozzleUseCase createNozzleUseCase,
                       ListNozzlesByPumpUseCase listNozzlesByPumpUseCase,
                       UpdatePumpUseCase updatePumpUseCase,
                       UpdatePumpStatusUseCase updatePumpStatusUseCase) {
        this.createNozzleUseCase = createNozzleUseCase;
        this.listNozzlesByPumpUseCase = listNozzlesByPumpUseCase;
        this.updatePumpUseCase = updatePumpUseCase;
        this.updatePumpStatusUseCase = updatePumpStatusUseCase;
    }

    @PutMapping("/{id}")
    public PumpResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePumpRequest request) {
        return PumpResponse.from(updatePumpUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/status")
    public PumpResponse updateStatus(@PathVariable UUID id, @RequestBody UpdatePumpStatusRequest request) {
        return PumpResponse.from(updatePumpStatusUseCase.execute(id, request));
    }

    @PostMapping("/{id}/nozzles")
    @ResponseStatus(HttpStatus.CREATED)
    public NozzleResponse createNozzle(@PathVariable UUID id, @Valid @RequestBody CreateNozzleRequest request) {
        return NozzleResponse.from(createNozzleUseCase.execute(id, request));
    }

    @GetMapping("/{id}/nozzles")
    public List<NozzleResponse> listNozzles(@PathVariable UUID id,
                                            @RequestParam(required = false) Boolean active) {
        return listNozzlesByPumpUseCase.execute(id, active).stream()
            .map(NozzleResponse::from)
            .toList();
    }
}
