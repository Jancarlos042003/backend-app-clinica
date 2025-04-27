package com.proyecto.appclinica.service;

import com.proyecto.appclinica.exception.ApiException;
import com.proyecto.appclinica.repository.FhirRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeVerificationService {
    private final FhirRepository fhirRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int CODE_LENGTH = 6;
    private static final long CODE_VALIDITY_MS = 5 * 60 * 1000L; // 5 minutos
    private static final String REDIS_CODE_PREFIX = "verification_code:";
    private static final String REDIS_ATTEMPTS_PREFIX = "verification_attempts:";
    private static final String REDIS_COOLDOWN_PREFIX = "verification_cooldown:";
    private static final int MAX_VERIFICATION_ATTEMPTS = 3;
    private static final long COOLDOWN_PERIOD_MS = 15 * 60 * 1000L; // 15 minutos de bloqueo

    /**
     * Genera y envía un código de verificación al usuario identificado
     * @param identifier Identificador único del usuario (ej: número de documento)
     * @throws ApiException si el usuario no existe o está en período de enfriamiento
     */
    public void generateAndSendCode(String identifier) {
        // Verificar que el usuario existe
        if (!fhirRepository.patientExistsByIdentifier(identifier)) {
            log.warn("Intento de generación de código para usuario inexistente: {}", identifier);
            throw new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }

        // Verificar si el usuario está en período de enfriamiento
        String cooldownKey = REDIS_COOLDOWN_PREFIX + identifier;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            log.warn("Usuario {} en período de enfriamiento. Tiempo restante: {} segundos", identifier, ttl);
            throw new ApiException("Demasiados intentos fallidos. Por favor, intente nuevamente más tarde",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        // Generar código
        String code = generateRandomCode();

        // Guardar el código en Redis con TTL de 5 minutos
        String redisKey = REDIS_CODE_PREFIX + identifier;
        redisTemplate.opsForValue().set(redisKey, code, CODE_VALIDITY_MS, TimeUnit.MILLISECONDS);

        // Reiniciar contador de intentos
        redisTemplate.delete(REDIS_ATTEMPTS_PREFIX + identifier);

        // Enviar código al usuario
        sendCodeToUser(identifier, code);
        log.info("Código de verificación generado para usuario: {}", identifier);
    }

    /**
     * Verifica si el código proporcionado es válido para el usuario
     * @param identifier Identificador único del usuario
     * @param inputCode Código ingresado para verificar
     * @return true si el código es válido, false de lo contrario
     * @throws ApiException si el usuario ha excedido el número máximo de intentos
     */
    public boolean verifyCode(String identifier, String inputCode) {
        // Verificar si el usuario está en período de enfriamiento
        String cooldownKey = REDIS_COOLDOWN_PREFIX + identifier;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            Long ttl = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
            log.warn("Usuario {} en período de enfriamiento durante verificación. Tiempo restante: {} segundos",
                    identifier, ttl);
            throw new ApiException("Demasiados intentos fallidos. Por favor, intente nuevamente más tarde",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        String redisKey = REDIS_CODE_PREFIX + identifier;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            log.info("Intento de verificación con código expirado o inexistente para usuario: {}", identifier);
            return false; // Código expirado o nunca existió
        }

        boolean isValid = storedCode.equals(inputCode);

        if (isValid) {
            // Invalidar el código después de un uso exitoso
            redisTemplate.delete(redisKey);
            // Reiniciar contador de intentos
            redisTemplate.delete(REDIS_ATTEMPTS_PREFIX + identifier);
            log.info("Verificación de código exitosa para usuario: {}", identifier);
        } else {
            // Incrementar contador de intentos fallidos
            String attemptsKey = REDIS_ATTEMPTS_PREFIX + identifier;
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

            // Establecer TTL al contador si es el primer intento
            if (attempts == 1) {
                redisTemplate.expire(attemptsKey, CODE_VALIDITY_MS, TimeUnit.MILLISECONDS);
            }

            log.warn("Intento fallido de verificación para usuario: {}. Intento {} de {}",
                    identifier, attempts, MAX_VERIFICATION_ATTEMPTS);

            // Si se alcanza el límite de intentos, activar período de enfriamiento
            if (attempts >= MAX_VERIFICATION_ATTEMPTS) {
                redisTemplate.opsForValue().set(cooldownKey, Instant.now().toString(),
                        COOLDOWN_PERIOD_MS, TimeUnit.MILLISECONDS);
                redisTemplate.delete(redisKey); // Invalidar el código actual
                redisTemplate.delete(attemptsKey); // Reiniciar contador

                log.warn("Usuario {} bloqueado por {} minutos debido a múltiples intentos fallidos",
                        identifier, COOLDOWN_PERIOD_MS / (60 * 1000));

                throw new ApiException("Número máximo de intentos excedido. Por favor, solicite un nuevo código después " +
                        "del período de enfriamiento", HttpStatus.TOO_MANY_REQUESTS);
            }
        }

        return isValid;
    }

    /**
     * Genera un código numérico aleatorio seguro
     * @return Código de verificación
     */
    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10)); // Dígitos 0-9
        }
        return sb.toString();
    }

    /**
     * Envía el código al usuario mediante el canal apropiado
     * @param identifier Identificador del usuario
     * @param code Código generado
     */
    private void sendCodeToUser(String identifier, String code) {
        // Implementar envío por SMS, email, etc.
        // Aquí solo simulamos el envío con un log
        log.info("SIMULACIÓN: Código para {}: {}", identifier, code);
    }
}