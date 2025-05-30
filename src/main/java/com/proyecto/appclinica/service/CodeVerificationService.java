package com.proyecto.appclinica.service;

import com.proyecto.appclinica.exception.ManyRequestsException;
import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.repository.FhirPatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeVerificationService {
    private final FhirPatientRepository fhirPatientRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SmsService smsService;
    private final EmailService emailService;

    private static final int CODE_LENGTH = 6;
    private static final long CODE_VALIDITY_MS = 5 * 60 * 1000L; // 5 minutos
    private static final String REDIS_CODE_PREFIX = "verification_code:";
    private static final String REDIS_ATTEMPTS_PREFIX = "verification_attempts:";
    private static final String REDIS_COOLDOWN_PREFIX = "verification_cooldown:";
    private static final int MAX_VERIFICATION_ATTEMPTS = 3;
    private static final long COOLDOWN_PERIOD_MS = 15 * 60 * 1000L; // 15 minutos de bloqueo

    public CodeSubmissionResponseDto generateAndSendCode(String identifier) {
        // Verifico existencia y cool-down
        Patient patient = findPatientOrThrow(identifier);

        // Verifico si está en período de enfriamiento
        checkCooldownOrThrow(identifier);

        // Obtengo datos
        String phoneNumber = getPhoneNumber(patient);
        String email = getEmail(patient);
        String fullName = extractFullName(patient);

        // Genero y guardo código
        String code = generateRandomCode();
        saveCodeInRedis(identifier, code);

        // Envío según disponibilidad
        boolean hasPhone = StringUtils.hasText(phoneNumber);
        boolean hasEmail = StringUtils.hasText(email);

        if (!hasPhone && !hasEmail) {
            log.warn("El paciente {} no tiene número de teléfono ni email registrado", identifier);
            throw new ResourceNotFoundException("Número de teléfono y Email");
        }
        if (hasPhone) {
            sendVerificationSms(phoneNumber, fullName, code);
            log.info("Enviando código por SMS al paciente {}", identifier);
        }
        if (hasEmail) {
            sendVerificationEmail(email, fullName, code);
            log.info("Enviando código por email al paciente {}", identifier);
        }

        log.info("El codigo es: {}", code);

        return new CodeSubmissionResponseDto(
                "El código ha sido enviado correctamente",
                hasPhone ? phoneNumber : null,
                hasEmail ? email : null
        );
    }

    public boolean verifyCode(String identifier, String inputCode) {
        // Cool-down
        checkCooldownOrThrow(identifier);

        // Obtener código almacenado
        String stored = redisTemplate.opsForValue().get(codeKey(identifier));
        if (!StringUtils.hasText(stored)) {
            log.info("Código expirado o inexistente para usuario: {}", identifier);
            return false;
        }

        // Comparar
        boolean valid = stored.equals(inputCode);
        if (valid) {
            onSuccessfulVerification(identifier);
        } else {
            onFailedVerification(identifier);
        }
        return valid;
    }

    public CodeSubmissionResponseDto resendVerificationCode(String identifier) {
        checkCooldownOrThrow(identifier);

        // Verificar si ya existe un código activo y si lo hay, eliminarlo
        String existingCode = redisTemplate.opsForValue().get(codeKey(identifier));
        if (StringUtils.hasText(existingCode)) {
            // Eliminamos el código anterior para generar uno nuevo
            redisTemplate.delete(codeKey(identifier));
            log.info("Eliminando código anterior para usuario: {}", identifier);
        }

        // Restablecer contador de intentos
        redisTemplate.delete(attemptsKey(identifier));

        // Generar y enviar un nuevo código
        return generateAndSendCode(identifier);
    }

    private Patient findPatientOrThrow(String id) {
        if (!fhirPatientRepository.patientExistsByIdentifier(id)) {
            log.warn("Usuario inexistente: {}", id);
            throw new ResourceNotFoundException("Usuario", "DNI", id);
        }
        return fhirPatientRepository.getPatientByIdentifier(id);
    }

    // Verificar si está en período de enfriamiento
    private void checkCooldownOrThrow(String id) {
        String key = REDIS_COOLDOWN_PREFIX + id;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.warn("Usuario {} en período de enfriamiento. TTL: {}s", id, ttl);

            throw new ManyRequestsException("Demasiados intentos fallidos. Intente más tarde");
        }
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10)); // Dígitos 0-9
        }
        return sb.toString();
    }

    private void saveCodeInRedis(String id, String code) {
        String key = REDIS_CODE_PREFIX + id;
        // 5 minutos de validez
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        // reset de intentos
        redisTemplate.delete(REDIS_ATTEMPTS_PREFIX + id);
    }

    private String extractFullName(Patient patient) {
        if (patient.getName().isEmpty()) {
            return "Paciente";
        }
        return patient.getNameFirstRep().getNameAsSingleString();
    }

    private String getPhoneNumber(Patient patient) {
        return patient.getTelecom().stream()
                .filter(t -> t.getSystem().name().equals("PHONE"))
                .findFirst()
                .map(t -> {
                    String phone = t.getValue();
                    // Asegurar que el número esté en formato E.164 (con +)
                    if (!phone.startsWith("+")) {
                        phone = "+" + phone.replaceAll("[^0-9]", "");
                    }
                    return phone;
                })
                .orElse(null);
    }

    private String getEmail(Patient patient) {
        return patient.getTelecom().stream()
                .filter(t -> t.getSystem().name().equals("EMAIL"))
                .findFirst()
                .map(ContactPoint::getValue)
                .orElse(null);
    }

    private void sendVerificationSms(String phoneNumber, String fullName, String verificationCode) {
        String msg = "PostCare: Hola, " + fullName.toUpperCase() + " tu código de verificación es: " + verificationCode;
        smsService.sendSms(phoneNumber, msg);
    }

    private void sendVerificationEmail(String to, String fullName, String verificationCode) {
        String body = "PostCare: Hola, " + fullName.toUpperCase() + " tu código de verificación es: " + verificationCode;
        emailService.sendEmail(to, "Código de verificación", body);
    }

    // Métodos para obtener las claves de Redis
    private String codeKey(String id)       { return REDIS_CODE_PREFIX + id; }
    private String attemptsKey(String id)   { return REDIS_ATTEMPTS_PREFIX + id; }
    private String cooldownKey(String id)   { return REDIS_COOLDOWN_PREFIX + id; }

    private void onSuccessfulVerification(String identifier) {
        redisTemplate.delete(codeKey(identifier));
        redisTemplate.delete(attemptsKey(identifier));
        log.info("Verificación de código exitosa para usuario: {}", identifier);
    }

    private void onFailedVerification(String identifier) {
        String aKey = attemptsKey(identifier);
        long attempts = redisTemplate.opsForValue().increment(aKey);

        if (attempts == 1) {
            redisTemplate.expire(aKey, CODE_VALIDITY_MS, TimeUnit.MILLISECONDS);
        }
        log.warn("Intento fallido {} de {} para usuario {}",
                attempts, MAX_VERIFICATION_ATTEMPTS, identifier);

        if (attempts >= MAX_VERIFICATION_ATTEMPTS) {
            // Bloquear
            redisTemplate.opsForValue()
                    .set(cooldownKey(identifier), Instant.now().toString(),
                            COOLDOWN_PERIOD_MS, TimeUnit.MILLISECONDS);
            redisTemplate.delete(codeKey(identifier));
            redisTemplate.delete(aKey);
            log.warn("Usuario {} bloqueado por {} min",
                    identifier, COOLDOWN_PERIOD_MS / 60000);

            throw new ManyRequestsException(
                    "Límite de intentos excedido. Inténtelo más tarde");
        }
    }
}