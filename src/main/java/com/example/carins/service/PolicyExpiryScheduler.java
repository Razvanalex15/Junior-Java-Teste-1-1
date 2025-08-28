package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

import java.util.List;

@Component
public class PolicyExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryScheduler.class);

    private final InsurancePolicyRepository policyRepository;

    public PolicyExpiryScheduler(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * Rulează la fiecare minut.
     * Presupunem că o poliță "expiră" la 00:00 a zilei de după endDate (expirat la miezul nopții).
     * Cerința spune: "log în max 1 oră după ce expiră" => logăm între 00:00 și 01:00 în ziua D+1.
     */
    @Scheduled(fixedDelay = 60_000) // la fiecare 60s
    public void logExpiredPolicies() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalTime nowTime = LocalTime.now(ZoneId.systemDefault());

        // căutăm polițele care au endDate = ieri și încă nu au fost logate
        LocalDate yesterday = today.minusDays(1);
        List<InsurancePolicy> candidates = policyRepository.findByEndDateAndExpiryLoggedFalse(yesterday);
        if (candidates.isEmpty()) return;

        // doar în prima oră după miezul nopții (00:00..01:00) — "at most 1 hour after"
        if (nowTime.isBefore(LocalTime.of(1, 0))) {
            for (InsurancePolicy p : candidates) {
                // mesajul cerut
                log.info("Policy {} for car {} expired on {}", p.getId(), p.getCar().getId(), p.getEndDate());

                // marcăm ca logată ca să nu spamăm următoarele rulări
                p.setExpiryLogged(true);
                policyRepository.save(p);
            }
        }
    }
}
