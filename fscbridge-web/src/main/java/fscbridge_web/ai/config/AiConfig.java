package fscbridge_web.ai.config;

import com.google.cloud.vertexai.VertexAI;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${spring.ai.vertex.ai.gemini.project-id}")
    private String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location}")
    private String location;

    @Bean
    public VertexAI vertexAI() {
        return new VertexAI.Builder()
                .setProjectId(projectId)
                .setLocation(location)
                .build();
    }

    @Bean
    public VertexAiGeminiChatModel geminiChatModel(VertexAI vertexAI) {
        return new VertexAiGeminiChatModel(vertexAI,
                VertexAiGeminiChatOptions.builder()
                        .model("gemini-1.5-flash")
                        .temperature(0.3)
                        .maxOutputTokens(1024)
                        .build());
    }
}
