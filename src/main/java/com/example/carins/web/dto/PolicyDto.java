package com.example.carins.web.dto;

import java.time.LocalDate;

public record PolicyDto(
        Long id,
        Long carId,
        String provider,
        LocalDate startDate,
        LocalDate endDate,
        Boolean expiryLogged
) {}
