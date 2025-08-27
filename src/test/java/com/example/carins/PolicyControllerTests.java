package com.example.carins;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyControllerTests {

  @Autowired MockMvc mvc;

  @Test
  void createPolicy_missingEndDate_returns400() throws Exception {
    String json = """
      { "provider":"TestCo", "startDate":"2025-01-01" }
      """;
    mvc.perform(post("/api/cars/1/policies")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createPolicy_forUnknownCar_returns404() throws Exception {
    String json = """
      { "provider":"TestCo", "startDate":"2025-01-01", "endDate":"2025-12-31" }
      """;
    mvc.perform(post("/api/cars/999/policies")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
      .andExpect(status().isNotFound());
  }
}
