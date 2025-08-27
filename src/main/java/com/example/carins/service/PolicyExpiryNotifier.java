package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class PolicyExpiryNotifier {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryNotifier.class);

    private final InsurancePolicyRepository policyRepo;

    public PolicyExpiryNotifier(InsurancePolicyRepository policyRepo) {
        this.policyRepo = policyRepo;
    }

    /**
     * Rulăm des (la fiecare minut), dar logăm NUMAI în fereastra 00:00–01:00,
     * pentru polițele al căror endDate a fost IERI. După log, setăm expiryLogged=true
     * ca să nu mai spamăm.
     */
    @Scheduled(cron = "0 * * * * *") // la fiecare minut
    @Transactional
    public void logRecentlyExpiredPolicies() {
        // Logăm în prima oră a zilei curente
        LocalTime nowTime = LocalTime.now();
        if (nowTime.isAfter(LocalTime.of(1, 0))) {
            return; // în afara ferestrei 00:00–01:00 nu facem nimic
        }

        LocalDate expiredYesterday = LocalDate.now().minusDays(1);

        List<InsurancePolicy> toLog = policyRepo.findByEndDateAndExpiryLoggedFalse(expiredYesterday);
        if (toLog.isEmpty()) return;

        for (InsurancePolicy p : toLog) {
            Long carId = (p.getCar() != null) ? p.getCar().getId() : null;
            log.info("Policy {} for car {} expired on {}", p.getId(), carId, p.getEndDate());
            p.setExpiryLogged(true); // marcare ca logată
        }

        // flush prin @Transactional; dacă preferi explicit:
        // policyRepo.saveAll(toLog);
    }
}
