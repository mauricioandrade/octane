package com.octane.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
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
