package com.whatishope.screenplay.llm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MockLlmClientTest {

    @Test
    void generateReturnsStableMockResult() {
        MockLlmClient client = new MockLlmClient();

        String result = client.generate("请生成角色列表");

        assertThat(result).contains("\"mock\": true");
        assertThat(result).contains("LLM_API_KEY is not configured");
        assertThat(result).contains("请生成角色列表");
    }
}
