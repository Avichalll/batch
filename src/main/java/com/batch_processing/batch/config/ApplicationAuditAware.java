package com.batch_processing.batch.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

public class ApplicationAuditAware implements AuditorAware<Integer> {

    @Override
    public Optional<Integer> getCurrentAuditor() {
        // Return a default auditor ID, e.g., system user ID
        return Optional.of(1); // Assuming 1 is the ID of the system user
    }
}
