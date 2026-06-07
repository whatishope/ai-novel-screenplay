package com.whatishope.screenplay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.ScenePlanningResponse;
import com.whatishope.screenplay.llm.LlmClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScenePlanningServiceTest {

    @Test
    void planParsesScenesFromLlmJsonAndNormalizesNumbering() {
        LlmClient llmClient = prompt -> """
                {
                  "scenes": [
                    {
                      "sceneId": "wrong_id",
                      "sceneNumber": 9,
                      "title": "雨夜委托",
                      "location": "林默事务所",
                      "time": "夜晚",
                      "characters": ["char_001"],
                      "summary": "许岚委托林默调查失踪案。",
                      "sourceChapter": 1
                    }
                  ]
                }
                """;
        ScenePlanningService service = new ScenePlanningService(llmClient, new ObjectMapper());

        ScenePlanningResponse response = service.plan(sampleChapters(), sampleCharacters());

        assertThat(response.scenes()).hasSize(1);
        assertThat(response.scenes().get(0).sceneId()).isEqualTo("scene_001");
        assertThat(response.scenes().get(0).sceneNumber()).isEqualTo(1);
        assertThat(response.scenes().get(0).title()).isEqualTo("雨夜委托");
    }

    @Test
    void planFallsBackToMockScenesWhenLlmReturnsUnstructuredResult() {
        ScenePlanningService service = new ScenePlanningService(prompt -> "mock result", new ObjectMapper());

        ScenePlanningResponse response = service.plan(sampleChapters(), sampleCharacters());

        assertThat(response.scenes()).hasSize(2);
        assertThat(response.scenes()).extracting("sceneId").containsExactly("scene_001", "scene_002");
        assertThat(response.scenes()).extracting("sceneNumber").containsExactly(1, 2);
        assertThat(response.scenes().get(0).characters()).containsExactly("char_001", "char_002");
    }

    @Test
    void planParsesScenesFromFencedLlmJson() {
        LlmClient llmClient = prompt -> """
                ```json
                {
                  "scenes": [
                    {
                      "sceneId": "scene_001",
                      "sceneNumber": 1,
                      "title": "旧钥匙",
                      "location": "事务所",
                      "time": "夜晚",
                      "characters": ["char_001", "char_002"],
                      "summary": "许岚交出旧钥匙，林默决定调查。",
                      "sourceChapter": 1
                    }
                  ]
                }
                ```
                """;
        ScenePlanningService service = new ScenePlanningService(llmClient, new ObjectMapper());

        ScenePlanningResponse response = service.plan(sampleChapters(), sampleCharacters());

        assertThat(response.scenes()).hasSize(1);
        assertThat(response.scenes().get(0).title()).isEqualTo("旧钥匙");
        assertThat(response.scenes().get(0).characters()).containsExactly("char_001", "char_002");
    }

    @Test
    void planFallsBackToMockScenesWhenLlmThrows() {
        ScenePlanningService service = new ScenePlanningService(
                prompt -> {
                    throw new IllegalStateException("LLM unavailable");
                },
                new ObjectMapper()
        );

        ScenePlanningResponse response = service.plan(sampleChapters(), sampleCharacters());

        assertThat(response.scenes()).hasSize(2);
        assertThat(response.scenes()).extracting("sceneId").containsExactly("scene_001", "scene_002");
    }

    @Test
    void planRejectsEmptyChapters() {
        ScenePlanningService service = new ScenePlanningService(prompt -> "{}", new ObjectMapper());

        assertThatThrownBy(() -> service.plan(List.of(), sampleCharacters()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Chapters must not be empty.");
    }

    @Test
    void planRejectsEmptyCharacters() {
        ScenePlanningService service = new ScenePlanningService(prompt -> "{}", new ObjectMapper());

        assertThatThrownBy(() -> service.plan(sampleChapters(), List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Characters must not be empty.");
    }

    private List<ChapterDto> sampleChapters() {
        return List.of(
                new ChapterDto(1, "第一章 雨夜", "许岚来到事务所。", 8),
                new ChapterDto(2, "第二章 仓库", "林默发现门锁被换。", 10)
        );
    }

    private List<CharacterDto> sampleCharacters() {
        return List.of(
                new CharacterDto("char_001", "林默", "年轻侦探", "冷静", "调查真相", 1),
                new CharacterDto("char_002", "许岚", "委托人", "焦急", "寻找哥哥", 1)
        );
    }
}
