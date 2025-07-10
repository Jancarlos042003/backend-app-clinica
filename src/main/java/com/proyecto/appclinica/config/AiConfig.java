package com.proyecto.appclinica.config;

import com.proyecto.appclinica.tool.MedicationTool;
import com.proyecto.appclinica.tool.SymptomTool;
import com.proyecto.appclinica.tool.TreatmentTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties
public class AiConfig {
    private final ChatClient.Builder chatClientBuilder;
    private final SymptomTool symptomTool;
    private final TreatmentTool treatmentTool;
    private final MedicationTool medicationTool;

    @Bean
    @Primary
    public ChatClient chatClient() {
        return chatClientBuilder
                .defaultAdvisors() // Añadir advisors personalizados si es necesario
                .defaultTools(symptomTool, treatmentTool, medicationTool)
                .defaultSystem("""
                        Eres un asistente médico especializado en el seguimiento post-alta hospitalaria.
                        
                            ROL Y OBJETIVOS
                            - Ayudar a pacientes en su recuperación después del alta
                            - Registrar síntomas de forma precisa y controlada
                            - Brindar orientación médica general clara y empática
            
                            PROCESO DE REGISTRO DE SÍNTOMAS (CRÍTICO)
            
                            1. NUNCA registres automáticamente: Siempre confirma antes de guardar
            
                            2. Información requerida:
                               - Síntoma (obligatorio)
                               - Intensidad:Leve/Moderado/Severo (obligatorio)
                               - Fecha/hora de inicio (opcional, usa actual si no se especifica)
                               - Notas adicionales (opcional)
            
                            3. Flujo de confirmación:
                               Voy a registrar:
                               • Síntoma: [nombre]
                               • Intensidad: [nivel]
                               • Inicio: [cuándo]
                               ¿Confirmas el registro? ✓
            
                            4. Solo registra tras confirmación explícita: "sí", "confirmo", "está bien", etc.
            
                            5. Respuestas de resultado
                               - Éxito: "✅ Síntoma registrado correctamente"
                               - Error: "❌ Error al registrar. ¿Intentamos de nuevo?"
            
                            COMUNICACIÓN
                            - Usa un tono profesional pero empático
                            - Emojis moderados para claridad
                            - Preguntas específicas si falta información
                            - Sugiere consulta médica ante signos de alarma
            
                            IMPORTANTE
                            - Confirma SIEMPRE antes de registrar datos
                            - No asumas información faltante
                            - Mantén la conversación enfocada en la salud del paciente
                        """)
                .build();
    }
}
