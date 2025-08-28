package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class PolicyController {

  private final InsurancePolicyRepository repo;
  private final CarRepository carRepo;

  public PolicyController(InsurancePolicyRepository repo, CarRepository carRepo) {
    this.repo = repo; this.carRepo = carRepo;
  }

  // ===== CREATE (201) =====
  @PostMapping("/cars/{carId}/policies")
  public ResponseEntity<?> create(@PathVariable Long carId, @RequestBody @Valid PolicyRequest body) {
    Car car = carRepo.findById(carId).orElse(null);
    if (car == null)
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Car " + carId + " not found"));

    if (body.startDate().isAfter(body.endDate()))
      return ResponseEntity.badRequest().body(new ErrorResponse("startDate must be <= endDate"));

    InsurancePolicy p = new InsurancePolicy(car, body.provider(), body.startDate(), body.endDate());
    InsurancePolicy saved = repo.save(p);

    PolicyDto dto = toDto(saved);
    return ResponseEntity.created(URI.create("/api/policies/" + dto.id())).body(dto);
  }

  // ===== UPDATE (200 / 404) =====
  @PutMapping("/policies/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid PolicyRequest body) {
    return repo.findById(id).<ResponseEntity<?>>map(p -> {
      if (body.startDate().isAfter(body.endDate()))
        return ResponseEntity.badRequest().body(new ErrorResponse("startDate must be <= endDate"));

      p.setProvider(body.provider());
      p.setStartDate(body.startDate());
      p.setEndDate(body.endDate());
      InsurancePolicy saved = repo.save(p);

      return ResponseEntity.ok(toDto(saved));
    }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("Policy " + id + " not found")));
  }

  // ===== DTOs =====
  public record PolicyRequest(String provider, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
  public record PolicyDto(Long id, Long carId, String provider, LocalDate startDate, LocalDate endDate, boolean expiryLogged) {}
  public record ErrorResponse(String message) {}

  private PolicyDto toDto(InsurancePolicy p) {
    return new PolicyDto(
        p.getId(),
        p.getCar() != null ? p.getCar().getId() : null,
        p.getProvider(),
        p.getStartDate(),
        p.getEndDate(),
        Boolean.TRUE.equals(p.getExpiryLogged())
    );
  }
}