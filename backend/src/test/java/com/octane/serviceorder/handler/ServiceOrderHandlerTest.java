package com.octane.serviceorder.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.octane.shared.auth.AuthenticatedUserService;
import com.octane.shared.exception.BusinessException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceOrderHandler.class)
class ServiceOrderHandlerTest {

    @MockitoBean
    private AuthenticatedUserService authService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateServiceOrderUseCase createServiceOrderUseCase;

    @MockitoBean
    private FindServiceOrderUseCase findServiceOrderUseCase;

    @MockitoBean
    private AddServiceOrderItemUseCase addServiceOrderItemUseCase;

    @MockitoBean
    private CloseServiceOrderUseCase closeServiceOrderUseCase;

    @MockitoBean
    private CancelServiceOrderUseCase cancelServiceOrderUseCase;

    @MockitoBean
    private ListServiceOrdersUseCase listServiceOrdersUseCase;

    @MockitoBean
    private GetVehicleHistoryUseCase getVehicleHistoryUseCase;

    private ServiceOrderResponse buildResponse(UUID id, UUID stationId, String status) {
        return new ServiceOrderResponse(
                id, stationId, "Posto X", "ABC1234", 50000,
                null, null, status, null,
                List.of(), BigDecimal.ZERO,
                LocalDateTime.now(), null, null, LocalDateTime.now()
        );
    }

    @Test
    void postServiceOrder_returns201_withValidRequest() throws Exception {
        var stationId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var response = buildResponse(orderId, stationId, "OPEN");

        when(createServiceOrderUseCase.execute(any(CreateServiceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/service-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateServiceOrderRequest(stationId, "ABC1234", 50000, null, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.plate").value("ABC1234"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void postServiceOrder_returns400_withMissingPlate() throws Exception {
        var stationId = UUID.randomUUID();

        mockMvc.perform(post("/api/service-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stationId\":\"" + stationId + "\",\"odometer\":50000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postClose_returns422_whenOrderClosed() throws Exception {
        var orderId = UUID.randomUUID();

        when(closeServiceOrderUseCase.execute(orderId))
                .thenThrow(new BusinessException("OS já foi encerrada ou cancelada"));

        mockMvc.perform(post("/api/service-orders/" + orderId + "/close"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("OS já foi encerrada ou cancelada"));
    }
}
