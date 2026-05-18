package com.taskanalysis.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatbotConfig {

    @Value("${chatbot.groq.api.key}")
    private String groqApiKey;

    @Value("${chatbot.groq.model}")
    private String model;

    @Value("${chatbot.max.tokens}")
    private Integer maxTokens;

    @Value("${chatbot.temperature}")
    private Double temperature;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")  // Groq endpoint
                .apiKey(groqApiKey)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(30))
                .logRequests(true)   // Debug logging
                .logResponses(true)
                .build();
    }
}
