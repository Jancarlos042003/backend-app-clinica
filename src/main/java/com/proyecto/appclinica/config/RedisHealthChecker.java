package com.proyecto.appclinica.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("redisConnectionFactory")  // Asegura que el bean Redis esté listo
public class RedisHealthChecker {

    private final RedisConnectionFactory redisConnectionFactory;

    @PostConstruct
    public void testRedisConnection() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            log.info("✅ Conexión a Redis establecida: {}", connection.ping());
            connection.close();
        } catch (Exception e) {
            log.error("❌ Error al conectar con Redis", e);
        }
    }
}
