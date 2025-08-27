package com.example.carins;

import com.example.carins.service.CarService;
import com.example.carins.web.CarController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CarController.class)
class CarControllerValidationTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean CarService carService;

    @Test
    @DisplayName("404 dacă mașina nu există")
    void insuranceValid_returns404_whenCarMissing() throws Exception {
        Mockito.when(carService.carExists(999L)).thenReturn(false);

        mvc.perform(get("/api/cars/{id}/insurance-valid", 999)
                        .param("date", "2025-06-01"))
           .andExpect(status().isNotFound())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.message").value("Car 999 not found"));
    }

    @Test
    @DisplayName("400 pentru format de dată invalid")
    void insuranceValid_returns400_whenDateBadFormat() throws Exception {
        Mockito.when(carService.carExists(1L)).thenReturn(true);

        mvc.perform(get("/api/cars/{id}/insurance-valid", 1)
                        .param("date", "2025-99-99"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid date format. Use YYYY-MM-DD."));
    }

    @Test
    @DisplayName("400 pentru dată în afara intervalului suportat")
    void insuranceValid_returns400_whenDateOutOfRange() throws Exception {
        Mockito.when(carService.carExists(1L)).thenReturn(true);

        mvc.perform(get("/api/cars/{id}/insurance-valid", 1)
                        .param("date", "1799-01-01"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message")
                   .value("Date out of supported range (1900-01-01 .. 2100-12-31)."));
    }

    @Test
    @DisplayName("200 + JSON corect pentru caz valid")
    void insuranceValid_ok() throws Exception {
        Mockito.when(carService.carExists(1L)).thenReturn(true);
        Mockito.when(carService.isInsuranceValid(eq(1L), any())).thenReturn(true);

        mvc.perform(get("/api/cars/{id}/insurance-valid", 1)
                        .param("date", "2025-06-01"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.carId").value(1))
           .andExpect(jsonPath("$.date").value("2025-06-01"))
           .andExpect(jsonPath("$.valid").value(true));
    }
}
