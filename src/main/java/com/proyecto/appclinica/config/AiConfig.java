package com.proyecto.appclinica.config;

import com.proyecto.appclinica.tool.MedicationTool;
import com.proyecto.appclinica.tool.SymptomTool;
import com.proyecto.appclinica.tool.TreatmentTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties
public class AiConfig {
    private final ChatClient.Builder chatClientBuilder;
    private final SymptomTool symptomTool;
    private final TreatmentTool treatmentTool;
    private final MedicationTool medicationTool;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

    @Value("classpath:prompts/instructions")
    private Resource promptBase;

    @Bean
    @Primary
    public ChatClient chatClient() {
        return chatClientBuilder
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultTools(symptomTool, treatmentTool, medicationTool)
                .defaultSystem(promptBase)
                .build();
    }
}
