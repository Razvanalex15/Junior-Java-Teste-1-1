package com.example.carins.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "insuranceclaim")
public class InsuranceClaim {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Car car;

    @NotNull
    @Column(nullable = false)
    private LocalDate claimDate;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    public InsuranceClaim() {}

    public InsuranceClaim(Car car, LocalDate claimDate, String description, BigDecimal amount) {
        this.car = car; this.claimDate = claimDate; this.description = description; this.amount = amount;
    }

    public Long getId() { return id; }
    public Car getCar() { return car; }
    public LocalDate getClaimDate() { return claimDate; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }

    public void setCar(Car car) { this.car = car; }
    public void setClaimDate(LocalDate claimDate) { this.claimDate = claimDate; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
