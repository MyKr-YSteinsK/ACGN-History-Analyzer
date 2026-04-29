package com.mykr.acgnhistoryanalyzer.llm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmClientConfig {

    @Bean
    public LlmClient llmClient(LlmProperties properties) {
        return new MockLlmClient(properties);
    }
}
