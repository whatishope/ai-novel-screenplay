package com.whatishope.screenplay.llm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LlmJsonResponseExtractorTest {

    @Test
    void extractsJsonFromMarkdownFence() {
        String content = """
                ```json
                {"characters":[{"id":"char_001","name":"林默"}]}
                ```
                """;

        String json = LlmJsonResponseExtractor.extractJsonObject(content);

        assertThat(json).isEqualTo("{\"characters\":[{\"id\":\"char_001\",\"name\":\"林默\"}]}");
    }

    @Test
    void extractsFirstBalancedJsonObjectFromText() {
        String content = """
                下面是结果：
                {"scenes":[{"title":"雨夜","summary":"门被敲响。"}]}
                请查收。
                """;

        String json = LlmJsonResponseExtractor.extractJsonObject(content);

        assertThat(json).isEqualTo("{\"scenes\":[{\"title\":\"雨夜\",\"summary\":\"门被敲响。\"}]}");
    }
}
