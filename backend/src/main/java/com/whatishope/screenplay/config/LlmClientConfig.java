package com.whatishope.screenplay.config;

import com.whatishope.screenplay.llm.HttpLlmClient;
import com.whatishope.screenplay.llm.LlmClient;
import com.whatishope.screenplay.llm.MockLlmClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmClientConfig {

    @Bean
    public LlmClient llmClient(LlmProperties properties, RestClient.Builder restClientBuilder) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            return new MockLlmClient();
        }
        return new HttpLlmClient(properties, restClientBuilder);
    }
}
