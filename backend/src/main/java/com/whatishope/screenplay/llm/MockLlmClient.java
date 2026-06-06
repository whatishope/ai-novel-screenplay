package com.whatishope.screenplay.llm;

public class MockLlmClient implements LlmClient {

    @Override
    public String generate(String prompt) {
        String safePrompt = prompt == null ? "" : prompt.strip();
        String preview = safePrompt.length() > 80 ? safePrompt.substring(0, 80) + "..." : safePrompt;
        return """
                {
                  "mock": true,
                  "message": "LLM_API_KEY is not configured. Returning mock generation result.",
                  "promptPreview": "%s"
                }
                """.formatted(escapeJson(preview)).strip();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
