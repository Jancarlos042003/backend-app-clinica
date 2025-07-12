package com.proyecto.appclinica.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RagConfiguration {
    private final VectorStore vectorStore;
    private final ChatModel model;


    @Bean
    public DocumentTransformer documentTransformer(){
        return new TokenTextSplitter(400, 150, 50, 200, true);
    }

    /**
     * Enriquece los metadatos de los documentos con palabras clave.
     * El número de palabras clave a extraer es 3.
     */
    @Bean
    public KeywordMetadataEnricher metadataEnricher(){
        return new KeywordMetadataEnricher(model, 3);
    }

    /**
     * Advisor para responder preguntas utilizando el vector store.
     * Configura la búsqueda para devolver los 4 resultados más relevantes
     * con un umbral de similitud del 0.7.
     */
    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(){
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(5)
                        .similarityThreshold(0.55)
                        .build())
                .build();
    }
}