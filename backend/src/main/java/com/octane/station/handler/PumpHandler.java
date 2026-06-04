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
