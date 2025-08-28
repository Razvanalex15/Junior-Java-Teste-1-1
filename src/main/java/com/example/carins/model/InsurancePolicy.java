package com.example.carins.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "insurancepolicy")
public class InsurancePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Car car;

    private String provider;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate; // obligatoriu

    // ✅ nou: folosit pentru Task D (cron-ul care loghează expirarea)
    @NotNull
    @Column(nullable = false)
    private Boolean expiryLogged = false;

    public InsurancePolicy() {}

    public InsurancePolicy(Car car, String provider, LocalDate startDate, LocalDate endDate) {
        this.car = car;
        this.provider = provider;
        this.startDate = startDate;
        this.endDate = endDate;
        this.expiryLogged = Boolean.FALSE;
    }

    public Long getId() { return id; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getExpiryLogged() { return expiryLogged; }
    public void setExpiryLogged(Boolean expiryLogged) { this.expiryLogged = expiryLogged; }
}
