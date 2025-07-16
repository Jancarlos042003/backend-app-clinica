package com.proyecto.appclinica.config;

import com.proyecto.appclinica.tool.MedicationTool;
import com.proyecto.appclinica.tool.SosTool;
import com.proyecto.appclinica.tool.SymptomTool;
import com.proyecto.appclinica.tool.TreatmentTool;
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
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

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
                .defaultTools(symptomTool, treatmentTool, medicationTool)
                .defaultSystem(promptBase)
                .build();
    }

    @Bean
    @Qualifier("sosChatClient")
    public ChatClient sosChatClient() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultTools(symptomTool, treatmentTool, medicationTool, sosTool)
                .defaultSystem(promptAiReportSos)
                .build();
    }
}
