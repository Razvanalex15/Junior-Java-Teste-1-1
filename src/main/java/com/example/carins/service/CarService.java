package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsuranceClaim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsuranceClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.ClaimDtos.HistoryEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;

    public CarService(CarRepository carRepository,
                      InsurancePolicyRepository policyRepository,
                      InsuranceClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;
        return policyRepository.existsActiveOnDate(carId, date);
    }

    public boolean carExists(Long id) {
        return carRepository.existsById(id);
    }

    public InsuranceClaim registerClaim(Long carId, LocalDate claimDate, String description, BigDecimal amount) {
        var car = carRepository.findById(carId).orElseThrow();
        var claim = new InsuranceClaim(car, claimDate, description, amount);
        return claimRepository.save(claim);
    }

    public List<HistoryEvent> history(Long carId) {
        var car = carRepository.findById(carId).orElseThrow();
        var events = new ArrayList<HistoryEvent>();

        policyRepository.findByCarId(car.getId()).forEach(p -> {
            events.add(new HistoryEvent("POLICY_START", p.getStartDate(),
                    "Policy " + p.getId() + (p.getProvider()!=null ? " ("+p.getProvider()+")": "") + " started"));
            events.add(new HistoryEvent("POLICY_END", p.getEndDate(),
                    "Policy " + p.getId() + " ends"));
        });

        claimRepository.findByCarIdOrderByClaimDateAsc(car.getId()).forEach(c -> {
            events.add(new HistoryEvent("CLAIM", c.getClaimDate(),
                    "Claim " + c.getId() + " amount=" + c.getAmount() + " - " + c.getDescription()));
        });

        events.sort(Comparator.comparing(HistoryEvent::date));
        return events;
    }
}
