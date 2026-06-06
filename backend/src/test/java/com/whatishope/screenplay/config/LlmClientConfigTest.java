package com.whatishope.screenplay.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.whatishope.screenplay.llm.HttpLlmClient;
import com.whatishope.screenplay.llm.LlmClient;
import com.whatishope.screenplay.llm.MockLlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class LlmClientConfigTest {

    private final LlmClientConfig config = new LlmClientConfig();

    @Test
    void createsMockClientWhenApiKeyIsBlank() {
        LlmProperties properties = new LlmProperties();
        properties.setApiKey("");

        LlmClient client = config.llmClient(properties, RestClient.builder());

        assertThat(client).isInstanceOf(MockLlmClient.class);
    }

    @Test
    void createsHttpClientWhenApiKeyExists() {
        LlmProperties properties = new LlmProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://example.com/v1");
        properties.setModel("test-model");

        LlmClient client = config.llmClient(properties, RestClient.builder());

        assertThat(client).isInstanceOf(HttpLlmClient.class);
    }
}
