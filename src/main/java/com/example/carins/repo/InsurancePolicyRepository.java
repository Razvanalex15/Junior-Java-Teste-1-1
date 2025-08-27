package com.example.carins.repo;

import com.example.carins.model.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    @Query("""
           select (count(p) > 0) from InsurancePolicy p
           where p.car.id = :carId
             and p.startDate <= :date
             and p.endDate >= :date
           """)
    boolean existsActiveOnDate(@Param("carId") Long carId, @Param("date") LocalDate date);

    List<InsurancePolicy> findByCarId(Long carId);

    // ---- Task D: pentru scheduler (polițe cu endDate = X și încă nelogate) ----
    List<InsurancePolicy> findByEndDateAndExpiryLoggedFalse(LocalDate endDate);
}
