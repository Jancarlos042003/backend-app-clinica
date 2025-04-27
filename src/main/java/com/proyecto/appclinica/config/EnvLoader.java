package com.proyecto.appclinica.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnvLoader {

    /**
     * This method is called after the bean is constructed to load environment variables.
     */

    String[] keys = {
            "DB_HOST", "DB_PORT", "DB_USER", "DB_PASSWORD", "DB_NAME",
            "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD",
            "SECRET_KEY", "EXPIRATION_TIME", "FHIR_SERVER_URL"
    };

    @PostConstruct
    public void loadEnv() {
        try {
            loadProfileVars(keys);
        } catch (Exception e) {
            log.error("Error loading environment variables: {}", e.getMessage());
        }
    }

    private void loadProfileVars(String... keys) {
        for (String key : keys) {
            String value = System.getenv(key);
            if (value != null) {
                System.setProperty(key, value);
            }
        }
    }
}
