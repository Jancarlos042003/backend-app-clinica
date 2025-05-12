package com.proyecto.appclinica.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    private final RedisConnectionFactory redisConnectionFactory;


    /**
     * Configura un {@link RedisConnectionFactory} para conectarse a Azure Redis.
     *
     * @return una instancia de {@link RedisConnectionFactory} configurada.
     */

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl()  // Habilitar SSL para Azure Redis
                .build();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        standaloneConfig.setPassword(RedisPassword.of(redisPassword));

        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
    }

    /**
     * Configura un {@link RedisTemplate} para interactuar con Redis.
     *
     * @return una instancia de {@link RedisTemplate} configurada.
     */

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // Usar serializadores de String para claves y valores
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        // También configurar serializadores para hash keys y values
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();

        return template;
    }

    // Prueba la conexión a Redis al iniciar la aplicación.
    @PostConstruct
    public void testRedisConnection() {
        RedisConnection connection = null;
        try {
            log.info("Intentando conectar a Redis en {}:{}", redisHost, redisPort);
            connection = redisConnectionFactory.getConnection();
            String pong = Objects.requireNonNull(connection.ping());
            log.info("Conexión a Redis establecida exitosamente. Respuesta: {}", pong);
        } catch (Exception e) {
            log.error("Error al conectar a Redis: {}", e.getMessage(), e);
            log.warn("La aplicación continuará ejecutándose, pero las funcionalidades de Redis no estarán disponibles");
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
