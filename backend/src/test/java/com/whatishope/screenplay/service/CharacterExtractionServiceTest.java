package com.whatishope.screenplay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.llm.LlmClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class CharacterExtractionServiceTest {

    @Test
    void extractParsesCharactersFromLlmJson() {
        LlmClient llmClient = prompt -> """
                {
                  "characters": [
                    {
                      "id": "char_001",
                      "name": "林默",
                      "description": "年轻侦探",
                      "personality": "冷静、敏锐",
                      "goal": "调查真相",
                      "firstAppearedChapter": 1
                    }
                  ]
                }
                """;
        CharacterExtractionService service = new CharacterExtractionService(llmClient, new ObjectMapper());

        CharacterExtractionResponse response = service.extract(List.of(
                new ChapterDto(1, "第一章 雨夜", "林默走进旧仓库。", 9)
        ));

        assertThat(response.characters()).hasSize(1);
        assertThat(response.characters().get(0).id()).isEqualTo("char_001");
        assertThat(response.characters().get(0).name()).isEqualTo("林默");
    }

    @Test
    void extractFallsBackToMockCharactersWhenLlmReturnsUnstructuredResult() {
        LlmClient llmClient = prompt -> "mock result";
        CharacterExtractionService service = new CharacterExtractionService(llmClient, new ObjectMapper());

        CharacterExtractionResponse response = service.extract(List.of(
                new ChapterDto(1, "第一章 雨夜", "许岚拿出旧钥匙。", 9),
                new ChapterDto(2, "第二章 仓库", "林默发现门锁被换。", 10)
        ));

        assertThat(response.characters()).hasSize(2);
        assertThat(response.characters()).extracting("id").containsExactly("char_001", "char_002");
        assertThat(response.characters()).extracting("name").containsExactly("林默", "许岚");
    }

    @Test
    void extractParsesCharactersFromFencedLlmJson() {
        LlmClient llmClient = prompt -> """
                ```json
                {
                  "characters": [
                    {
                      "id": "char_001",
                      "name": "沈青",
                      "description": "雨夜来客",
                      "personality": "谨慎",
                      "goal": "寻找旧信",
                      "firstAppearedChapter": 1
                    }
                  ]
                }
                ```
                """;
        CharacterExtractionService service = new CharacterExtractionService(llmClient, new ObjectMapper());

        CharacterExtractionResponse response = service.extract(List.of(
                new ChapterDto(1, "第一章 雨夜", "沈青站在门外。", 8)
        ));

        assertThat(response.characters()).hasSize(1);
        assertThat(response.characters().get(0).name()).isEqualTo("沈青");
    }

    @Test
    void extractFallsBackToMockCharactersWhenLlmThrows() {
        CharacterExtractionService service = new CharacterExtractionService(
                prompt -> {
                    throw new IllegalStateException("LLM unavailable");
                },
                new ObjectMapper()
        );

        CharacterExtractionResponse response = service.extract(List.of(
                new ChapterDto(1, "第一章 雨夜", "许岚拿出旧钥匙。", 9)
        ));

        assertThat(response.characters()).hasSize(2);
        assertThat(response.characters()).extracting("name").containsExactly("林默", "许岚");
    }

    @Test
    void extractRejectsEmptyChapters() {
        CharacterExtractionService service = new CharacterExtractionService(prompt -> "{}", new ObjectMapper());

        assertThatThrownBy(() -> service.extract(List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Chapters must not be empty.");
    }

    @Test
    void extractRejectsBlankChapterContent() {
        CharacterExtractionService service = new CharacterExtractionService(prompt -> "{}", new ObjectMapper());

        assertThatThrownBy(() -> service.extract(List.of(
                        new ChapterDto(1, "第一章", " ", 0)
                )))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Each chapter must contain content.");
    }
}
