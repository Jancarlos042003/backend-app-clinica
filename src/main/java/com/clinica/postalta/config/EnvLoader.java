package com.clinica.postalta.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnvLoader {
    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // Load the relevant variables for the active profile
            String activeProfile = System.getProperty("spring.profiles.active", "dev");
            log.info("Active profile: {}", activeProfile);

            if ("dev".equals(activeProfile)) {
                loadProfileVars(dotenv, "DB_HOST_DEV", "DB_PORT_DEV", "DB_USER_DEV", "DB_PASSWORD_DEV");
            } else if ("prod".equals(activeProfile)) {
                loadProfileVars(dotenv, "DB_HOST_PROD", "DB_PORT_PROD", "DB_USER_PROD", "DB_PASSWORD_PROD");
            }
        } catch (Exception e) {
            log.error("Error loading environment variables: {}", e.getMessage());
        }
    }

    private void loadProfileVars(Dotenv dotenv, String... keys) {
        for (String key : keys) {
            String value = dotenv.get(key);
            if (value != null) {
                System.setProperty(key, value);
            }
        }
    }
}

