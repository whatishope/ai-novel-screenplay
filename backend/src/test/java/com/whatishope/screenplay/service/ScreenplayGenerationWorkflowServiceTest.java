package com.whatishope.screenplay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.ChapterSplitResponse;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.dto.SceneDto;
import com.whatishope.screenplay.dto.ScenePlanningResponse;
import com.whatishope.screenplay.dto.ScreenplayFullGenerationResponse;
import com.whatishope.screenplay.dto.ScreenplayYamlGenerationResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScreenplayGenerationWorkflowServiceTest {

    @Mock
    private ChapterSplitService chapterSplitService;

    @Mock
    private CharacterExtractionService characterExtractionService;

    @Mock
    private ScenePlanningService scenePlanningService;

    @Mock
    private YamlGenerationService yamlGenerationService;

    @InjectMocks
    private ScreenplayGenerationWorkflowService service;

    @Test
    void generateRunsFullWorkflow() {
        List<ChapterDto> chapters = List.of(new ChapterDto(1, "Chapter 1", "A visitor arrives.", 17));
        List<CharacterDto> characters = List.of(
                new CharacterDto("char_001", "Lin", "Detective", "Calm", "Find truth", 1)
        );
        List<SceneDto> scenes = List.of(
                new SceneDto(
                        "scene_001",
                        1,
                        "Opening",
                        "Office",
                        "Night",
                        List.of("char_001"),
                        "Lin meets the visitor.",
                        1
                )
        );

        when(chapterSplitService.split("Chapter 1\nA visitor arrives."))
                .thenReturn(new ChapterSplitResponse(1, chapters));
        when(characterExtractionService.extract(chapters))
                .thenReturn(new CharacterExtractionResponse(characters));
        when(scenePlanningService.plan(chapters, characters))
                .thenReturn(new ScenePlanningResponse(scenes));
        when(yamlGenerationService.generate("Demo", chapters, characters, scenes))
                .thenReturn(new ScreenplayYamlGenerationResponse("metadata:\n  title: Demo\n", 1, 1));

        ScreenplayFullGenerationResponse response = service.generate("Demo", "Chapter 1\nA visitor arrives.");

        assertThat(response.chapterCount()).isEqualTo(1);
        assertThat(response.characterCount()).isEqualTo(1);
        assertThat(response.sceneCount()).isEqualTo(1);
        assertThat(response.chapters()).isEqualTo(chapters);
        assertThat(response.characters()).isEqualTo(characters);
        assertThat(response.scenes()).isEqualTo(scenes);
        assertThat(response.yamlText()).contains("metadata:");

        verify(chapterSplitService).split("Chapter 1\nA visitor arrives.");
        verify(characterExtractionService).extract(chapters);
        verify(scenePlanningService).plan(chapters, characters);
        verify(yamlGenerationService).generate("Demo", chapters, characters, scenes);
    }
}
