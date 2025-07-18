package com.proyecto.appclinica.config;

import com.proyecto.appclinica.tool.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final ChatModel chatModel;
    private final SymptomTool symptomTool;
    private final TreatmentTool treatmentTool;
    private final MedicationTool medicationTool;
    private final SosTool sosTool;
    private final DateTimeTool dateTimeTool;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final EmergencyContactTool emergencyContactTool;
    private final PatientHistoryTool patientHistoryTool;

    @Value("classpath:prompts/general-chat")
    private Resource promptBase;

    @Value("classpath:prompts/ai-report-sos")
    private Resource promptAiReportSos;

    @Bean
    @Primary
    @Qualifier("generalChatClient")
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultTools(symptomTool, treatmentTool, medicationTool, dateTimeTool, patientHistoryTool)
                .defaultSystem(promptBase)
                .build();
    }

    @Bean
    @Qualifier("sosChatClient")
    public ChatClient sosChatClient() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultTools(symptomTool, treatmentTool, medicationTool, sosTool,
                        dateTimeTool, emergencyContactTool, patientHistoryTool)
                .defaultSystem(promptAiReportSos)
                .build();
    }
}
