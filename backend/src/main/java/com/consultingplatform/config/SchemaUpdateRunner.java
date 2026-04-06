package com.consultingplatform.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Drops the NOT NULL constraint on consulting_services.consultant_id so that
 * admin-created platform services (which have no associated consultant) can be saved.
 * Safe to run on every startup — PostgreSQL ignores it if the column is already nullable.
 */
@Component
public class SchemaUpdateRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public SchemaUpdateRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                "ALTER TABLE consulting_services ALTER COLUMN consultant_id DROP NOT NULL"
            );
        } catch (Exception ignored) {
            // Already nullable or column does not exist — nothing to do
        }
    }
}
