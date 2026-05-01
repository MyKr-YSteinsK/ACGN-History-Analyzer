package com.mykr.acgnhistoryanalyzer.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmClientConfig {

    @Bean
    public LlmClient llmClient(LlmProperties properties, ObjectMapper objectMapper) {
        return new RuntimeSwitchingLlmClient(properties, objectMapper);
    }
}
