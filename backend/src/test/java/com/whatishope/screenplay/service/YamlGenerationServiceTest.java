package com.whatishope.screenplay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.SceneDto;
import com.whatishope.screenplay.dto.ScreenplayYamlGenerationResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class YamlGenerationServiceTest {

    private final YamlGenerationService service = new YamlGenerationService();

    @Test
    void generateReturnsStructuredYaml() {
        ScreenplayYamlGenerationResponse response = service.generate(
                "雨夜来客",
                sampleChapters(),
                sampleCharacters(),
                sampleScenes()
        );

        assertThat(response.sceneCount()).isEqualTo(1);
        assertThat(response.characterCount()).isEqualTo(2);
        assertThat(response.yamlText()).contains("metadata:");
        assertThat(response.yamlText()).contains("characters:");
        assertThat(response.yamlText()).contains("scenes:");
        assertThat(response.yamlText()).contains("source_trace:");

        Map<?, ?> parsed = new Yaml().load(response.yamlText());
        assertThat(parsed.containsKey("metadata")).isTrue();
        assertThat(parsed.containsKey("characters")).isTrue();
        assertThat(parsed.containsKey("relationships")).isTrue();
        assertThat(parsed.containsKey("scenes")).isTrue();
        assertThat(parsed.containsKey("production")).isTrue();

        List<?> relationships = (List<?>) parsed.get("relationships");
        assertThat(relationships).hasSize(1);
        Map<?, ?> relationship = (Map<?, ?>) relationships.get(0);
        assertThat(relationship.get("from")).isEqualTo("char_001");
        assertThat(relationship.get("to")).isEqualTo("char_002");
        assertThat(relationship.get("type")).isEqualTo("共现");
        assertThat((String) relationship.get("description")).contains("林默", "许岚", "雨夜委托");
    }

    @Test
    void generateRejectsEmptyChapters() {
        assertThatThrownBy(() -> service.generate("标题", List.of(), sampleCharacters(), sampleScenes()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Chapters must not be empty.");
    }

    @Test
    void generateRejectsEmptyCharacters() {
        assertThatThrownBy(() -> service.generate("标题", sampleChapters(), List.of(), sampleScenes()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Characters must not be empty.");
    }

    @Test
    void generateRejectsEmptyScenes() {
        assertThatThrownBy(() -> service.generate("标题", sampleChapters(), sampleCharacters(), List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Scenes must not be empty.");
    }

    private List<ChapterDto> sampleChapters() {
        return List.of(new ChapterDto(1, "第一章 雨夜", "许岚来到事务所。", 8));
    }

    private List<CharacterDto> sampleCharacters() {
        return List.of(
                new CharacterDto("char_001", "林默", "年轻侦探", "冷静", "调查真相", 1),
                new CharacterDto("char_002", "许岚", "委托人", "焦急", "寻找哥哥", 1)
        );
    }

    private List<SceneDto> sampleScenes() {
        return List.of(new SceneDto(
                "scene_001",
                1,
                "雨夜委托",
                "林默事务所",
                "夜晚",
                List.of("char_001", "char_002"),
                "许岚委托林默调查失踪案。",
                1
        ));
    }
}
