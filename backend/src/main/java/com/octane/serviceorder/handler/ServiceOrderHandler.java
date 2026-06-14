package com.octane.serviceorder.handler;

import com.octane.serviceorder.usecase.AddServiceOrderItemRequest;
import com.octane.serviceorder.usecase.AddServiceOrderItemUseCase;
import com.octane.serviceorder.usecase.CancelServiceOrderUseCase;
import com.octane.serviceorder.usecase.CloseServiceOrderUseCase;
import com.octane.serviceorder.usecase.CreateServiceOrderRequest;
import com.octane.serviceorder.usecase.CreateServiceOrderUseCase;
import com.octane.serviceorder.usecase.FindServiceOrderUseCase;
import com.octane.serviceorder.usecase.GetVehicleHistoryUseCase;
import com.octane.serviceorder.usecase.ListServiceOrdersUseCase;
import com.octane.serviceorder.usecase.ServiceOrderResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class ServiceOrderHandler {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final FindServiceOrderUseCase findServiceOrderUseCase;
    private final AddServiceOrderItemUseCase addServiceOrderItemUseCase;
    private final CloseServiceOrderUseCase closeServiceOrderUseCase;
    private final CancelServiceOrderUseCase cancelServiceOrderUseCase;
    private final ListServiceOrdersUseCase listServiceOrdersUseCase;
    private final GetVehicleHistoryUseCase getVehicleHistoryUseCase;

    public ServiceOrderHandler(
            CreateServiceOrderUseCase createServiceOrderUseCase,
            FindServiceOrderUseCase findServiceOrderUseCase,
            AddServiceOrderItemUseCase addServiceOrderItemUseCase,
            CloseServiceOrderUseCase closeServiceOrderUseCase,
            CancelServiceOrderUseCase cancelServiceOrderUseCase,
            ListServiceOrdersUseCase listServiceOrdersUseCase,
            GetVehicleHistoryUseCase getVehicleHistoryUseCase
    ) {
        this.createServiceOrderUseCase = createServiceOrderUseCase;
        this.findServiceOrderUseCase = findServiceOrderUseCase;
        this.addServiceOrderItemUseCase = addServiceOrderItemUseCase;
        this.closeServiceOrderUseCase = closeServiceOrderUseCase;
        this.cancelServiceOrderUseCase = cancelServiceOrderUseCase;
        this.listServiceOrdersUseCase = listServiceOrdersUseCase;
        this.getVehicleHistoryUseCase = getVehicleHistoryUseCase;
    }

    @PostMapping("/api/service-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse create(@Valid @RequestBody CreateServiceOrderRequest request) {
        return createServiceOrderUseCase.execute(request);
    }

    @GetMapping("/api/service-orders/{id}")
    public ServiceOrderResponse find(@PathVariable UUID id) {
        return findServiceOrderUseCase.execute(id);
    }

    @PostMapping("/api/service-orders/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse addItem(@PathVariable UUID id,
                                        @Valid @RequestBody AddServiceOrderItemRequest request) {
        return addServiceOrderItemUseCase.execute(id, request);
    }

    @PostMapping("/api/service-orders/{id}/close")
    public ServiceOrderResponse close(@PathVariable UUID id) {
        return closeServiceOrderUseCase.execute(id);
    }

    @PostMapping("/api/service-orders/{id}/cancel")
    public ServiceOrderResponse cancel(@PathVariable UUID id) {
        return cancelServiceOrderUseCase.execute(id);
    }

    @GetMapping("/api/stations/{stationId}/service-orders")
    public List<ServiceOrderResponse> listByStation(
            @PathVariable UUID stationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return listServiceOrdersUseCase.execute(stationId, status, from, to);
    }

    @GetMapping("/api/service-orders/by-plate/{plate}")
    public List<ServiceOrderResponse> byPlate(@PathVariable String plate) {
        return getVehicleHistoryUseCase.execute(plate);
    }
}
