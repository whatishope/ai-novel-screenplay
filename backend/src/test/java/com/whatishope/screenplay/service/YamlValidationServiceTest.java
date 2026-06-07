package com.whatishope.screenplay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ScreenplayYamlValidationResponse;
import org.junit.jupiter.api.Test;

class YamlValidationServiceTest {

    private final YamlValidationService service = new YamlValidationService();

    @Test
    void validateAcceptsStructuredYaml() {
        ScreenplayYamlValidationResponse response = service.validate(validYaml());

        assertThat(response.valid()).isTrue();
        assertThat(response.errors()).isEmpty();
        assertThat(response.warnings()).contains("relationships is empty; character relationship graph will be empty.");
        assertThat(response.sceneCount()).isEqualTo(1);
        assertThat(response.characterCount()).isEqualTo(1);
        assertThat(response.relationshipCount()).isZero();
    }

    @Test
    void validateReturnsWarningsWithoutFailing() {
        String yaml = """
                metadata:
                  title: Demo
                  language: zh-CN
                  chapter_count: 1
                  version: "1.0"
                characters:
                  - id: char_001
                    name: Lin
                scenes:
                  - scene_id: scene_001
                    scene_number: 1
                    title: Opening
                    summary: Lin enters the room.
                """;

        ScreenplayYamlValidationResponse response = service.validate(yaml);

        assertThat(response.valid()).isTrue();
        assertThat(response.errors()).isEmpty();
        assertThat(response.warnings()).contains(
                "relationships is missing; character relationship graph will be empty.",
                "production is missing; export will not include production notes.",
                "scenes[0].source_trace is missing; generated screenplay may be less traceable.",
                "scenes[0].actions is missing; generated screenplay may be less traceable.",
                "scenes[0].dialogues is missing; generated screenplay may be less traceable."
        );
        assertThat(response.relationshipCount()).isZero();
    }

    @Test
    void validateReportsMissingRequiredFields() {
        String yaml = """
                metadata:
                  title: Demo
                characters: []
                scenes: []
                """;

        ScreenplayYamlValidationResponse response = service.validate(yaml);

        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).contains(
                "metadata.language is required and must be a non-empty string.",
                "metadata.chapter_count is required and must be a number.",
                "metadata.version is required and must be a non-empty string.",
                "characters must not be empty.",
                "scenes must not be empty."
        );
    }

    @Test
    void validateReportsUnknownCharacterReferences() {
        String yaml = """
                metadata:
                  title: Demo
                  language: zh-CN
                  chapter_count: 1
                  version: "1.0"
                characters:
                  - id: char_001
                    name: Lin
                scenes:
                  - scene_id: scene_001
                    scene_number: 1
                    title: Opening
                    characters:
                      - char_999
                    summary: Lin enters the room.
                    dialogues:
                      - order: 1
                        character: char_999
                        content: Who is there?
                """;

        ScreenplayYamlValidationResponse response = service.validate(yaml);

        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).contains(
                "scenes[0].characters[0] references unknown character 'char_999'.",
                "scenes[0].dialogues[0].character references unknown character 'char_999'."
        );
    }

    @Test
    void validateReportsDuplicateSceneIds() {
        String yaml = """
                metadata:
                  title: Demo
                  language: zh-CN
                  chapter_count: 1
                  version: "1.0"
                characters:
                  - id: char_001
                    name: Lin
                scenes:
                  - scene_id: scene_001
                    scene_number: 1
                    title: Opening
                    summary: Lin enters the room.
                  - scene_id: scene_001
                    scene_number: 2
                    title: Return
                    summary: Lin returns to the room.
                """;

        ScreenplayYamlValidationResponse response = service.validate(yaml);

        assertThat(response.valid()).isFalse();
        assertThat(response.errors())
                .contains("scenes[1].scene_id duplicates scene id 'scene_001'.");
    }

    @Test
    void validateReportsOutOfRangeSourceTraceChapterIndex() {
        String yaml = """
                metadata:
                  title: Demo
                  language: zh-CN
                  chapter_count: 1
                  version: "1.0"
                characters:
                  - id: char_001
                    name: Lin
                scenes:
                  - scene_id: scene_001
                    scene_number: 1
                    title: Opening
                    characters:
                      - char_001
                    summary: Lin enters the room.
                    actions:
                      - order: 1
                        content: Lin checks the window.
                        source_trace:
                          chapter_index: 0
                    dialogues:
                      - order: 1
                        character: char_001
                        content: Who is there?
                        source_trace:
                          chapter_index: 2
                    source_trace:
                      chapter_index: 2
                """;

        ScreenplayYamlValidationResponse response = service.validate(yaml);

        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).contains(
                "scenes[0].actions[0].source_trace.chapter_index must be between 1 and metadata.chapter_count (1).",
                "scenes[0].dialogues[0].source_trace.chapter_index must be between 1 and metadata.chapter_count (1).",
                "scenes[0].source_trace.chapter_index must be between 1 and metadata.chapter_count (1)."
        );
    }

    @Test
    void validateReportsInvalidRelationships() {
        String yaml = """
                metadata:
                  title: Demo
                  language: zh-CN
                  chapter_count: 1
                  version: "1.0"
                characters:
                  - id: char_001
                    name: Lin
                relationships:
                  - from: char_001
                    to: char_999
                    type: 同伴
                  - from: ""
                    to: char_001
                    type: ""
                  - broken
                scenes:
                  - scene_id: scene_001
                    scene_number: 1
                    title: Opening
                    summary: Lin enters the room.
                """;

        ScreenplayYamlValidationResponse response = service.validate(yaml);

        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).contains(
                "relationships[0].to references unknown character 'char_999'.",
                "relationships[1].from is required and must be a non-empty string.",
                "relationships[1].type is required and must be a non-empty string.",
                "relationships[2] must be an object."
        );
        assertThat(response.relationshipCount()).isEqualTo(3);
    }

    @Test
    void validateReportsYamlSyntaxErrors() {
        ScreenplayYamlValidationResponse response = service.validate("metadata:\n  title: Demo\n  - broken");

        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).hasSize(1);
        assertThat(response.warnings()).isEmpty();
        assertThat(response.errors().get(0)).startsWith("YAML syntax error:");
    }

    @Test
    void validateRejectsEmptyYamlText() {
        assertThatThrownBy(() -> service.validate(" "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("YAML text must not be empty.");
    }

    private String validYaml() {
        return """
                metadata:
                  title: Demo
                  language: zh-CN
                  chapter_count: 1
                  version: "1.0"
                characters:
                  - id: char_001
                    name: Lin
                relationships: []
                scenes:
                  - scene_id: scene_001
                    scene_number: 1
                    title: Opening
                    characters:
                      - char_001
                    summary: Lin enters the room.
                    actions:
                      - order: 1
                        content: Lin checks the window.
                        source_trace:
                          chapter_index: 1
                    dialogues:
                      - order: 1
                        character: char_001
                        content: Who is there?
                        source_trace:
                          chapter_index: 1
                    source_trace:
                      chapter_index: 1
                production:
                  target_format: short
                """;
    }
}
