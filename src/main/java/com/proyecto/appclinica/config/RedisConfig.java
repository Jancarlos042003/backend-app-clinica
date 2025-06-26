package com.proyecto.appclinica.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.timeout:5000}")
    private long timeout;

    @Value("${spring.data.redis.database:0}")
    private int database;

    /**
     * Configura un {@link RedisConnectionFactory} para conectarse a Azure Redis.
     *
     * @return una instancia de {@link RedisConnectionFactory} configurada.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofMillis(timeout))
                        .shutdownTimeout(Duration.ofMillis(timeout));

        // Solo habilitar SSL si está configurado (Azure/AWS)
        if (sslEnabled) {
            clientConfigBuilder.useSsl();
            log.info("SSL habilitado para Redis en host: {}", redisHost);
        } else {
            log.info("Conectando a Redis sin SSL en host: {}", redisHost);
        }

        LettuceClientConfiguration clientConfig = clientConfigBuilder.build();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        standaloneConfig.setDatabase(database);

        // Solo configurar password si no está vacío
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            standaloneConfig.setPassword(RedisPassword.of(redisPassword));
            log.info("Password configurado para Redis");
        } else {
            log.info("Conectando a Redis sin password");
        }

        log.info("Configurando Redis: host={}, port={}, database={}, ssl={}",
                redisHost, redisPort, database, sslEnabled);

        return new LettuceConnectionFactory(standaloneConfig, clientConfig);
    }

    /**
     * Configura un {@link RedisTemplate} para interactuar con Redis usando String/String.
     *
     * @return una instancia de {@link RedisTemplate} configurada.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Usar serializadores de String para claves y valores
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        log.info("RedisTemplate configurado correctamente");

        return template;
    }

    /**
     * Configura un {@link StringRedisTemplate} para operaciones simples con Redis.
     * Más eficiente para operaciones básicas String/String.
     *
     * @return una instancia de {@link StringRedisTemplate} configurada.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        log.info("StringRedisTemplate configurado correctamente");
        return template;
    }
}
