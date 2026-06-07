package com.whatishope.screenplay.llm;

import org.springframework.util.StringUtils;

public final class LlmJsonResponseExtractor {

    private LlmJsonResponseExtractor() {
    }

    public static String extractJsonObject(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        String stripped = stripCodeFence(content.strip());
        int start = stripped.indexOf('{');
        if (start < 0) {
            return stripped;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < stripped.length(); index++) {
            char current = stripped.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return stripped.substring(start, index + 1);
                }
            }
        }

        return stripped;
    }

    private static String stripCodeFence(String content) {
        if (!content.startsWith("```")) {
            return content;
        }

        int firstLineEnd = content.indexOf('\n');
        int closingFence = content.lastIndexOf("```");
        if (firstLineEnd < 0 || closingFence <= firstLineEnd) {
            return content;
        }

        return content.substring(firstLineEnd + 1, closingFence).strip();
    }
}
