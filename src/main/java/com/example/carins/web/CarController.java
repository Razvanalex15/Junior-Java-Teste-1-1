package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.ClaimDtos.ClaimDto;
import com.example.carins.web.dto.ClaimDtos.ClaimRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) { this.service = service; }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId,
                                              @RequestParam String date) {
        if (!service.carExists(carId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Car " + carId + " not found"));
        }
        final LocalDate d;
        try { d = LocalDate.parse(date); }
        catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid date format. Use YYYY-MM-DD."));
        }
        if (d.isBefore(LocalDate.of(1900,1,1)) || d.isAfter(LocalDate.of(2100,12,31))) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Date out of supported range (1900-01-01 .. 2100-12-31)."));
        }
        boolean valid = service.isInsuranceValid(carId, d);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, d.toString(), valid));
    }

    @PostMapping("/{carId}/claims")
    public ResponseEntity<InsuranceClaim> registerClaim(
        @PathVariable Long carId,
        @Valid @RequestBody ClaimDto req) {

    InsuranceClaim created = service.registerClaim(
            carId,
            req.claimDate(),
            req.description(),
            req.amount()
    );

    URI location = URI.create(String.format("/api/cars/%d/claims/%d", carId, created.getId()));
    return ResponseEntity.created(location).body(created);
}

    @GetMapping("/cars/{carId}/history")
    public ResponseEntity<?> carHistory(@PathVariable Long carId) {
        if (!service.carExists(carId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Car " + carId + " not found"));
        }
        return ResponseEntity.ok(service.history(carId));
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
    public record ErrorResponse(String message) {}
}
