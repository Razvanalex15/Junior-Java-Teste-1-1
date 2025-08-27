package com.example.carins.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ClaimDtos {
    // request body pentru POST /claims
    public record ClaimRequest(
            @NotNull LocalDate claimDate,
            @NotBlank @Size(max = 1000) String description,
            @NotNull @Positive BigDecimal amount
    ) {}

    // răspuns după crearea unui claim
    public record ClaimDto(
            Long id, Long carId, LocalDate claimDate, String description, BigDecimal amount
    ) {}

    // element din istoricul unei mașini
    public record HistoryEvent(
            String type, LocalDate date, String summary
    ) {}
}
