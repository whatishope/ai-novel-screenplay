package com.whatishope.screenplay.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.whatishope.screenplay.common.GlobalExceptionHandler;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.dto.SceneDto;
import com.whatishope.screenplay.dto.ScenePlanningResponse;
import com.whatishope.screenplay.dto.ScreenplayFullGenerationResponse;
import com.whatishope.screenplay.dto.ScreenplayYamlGenerationResponse;
import com.whatishope.screenplay.dto.ScreenplayYamlValidationResponse;
import com.whatishope.screenplay.service.CharacterExtractionService;
import com.whatishope.screenplay.service.ScenePlanningService;
import com.whatishope.screenplay.service.ScreenplayGenerationWorkflowService;
import com.whatishope.screenplay.service.YamlGenerationService;
import com.whatishope.screenplay.service.YamlValidationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScreenplayController.class)
@Import(GlobalExceptionHandler.class)
class ScreenplayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CharacterExtractionService characterExtractionService;

    @MockBean
    private ScenePlanningService scenePlanningService;

    @MockBean
    private YamlGenerationService yamlGenerationService;

    @MockBean
    private YamlValidationService yamlValidationService;

    @MockBean
    private ScreenplayGenerationWorkflowService screenplayGenerationWorkflowService;

    @Test
    void extractCharactersReturnsCharacterList() throws Exception {
        when(characterExtractionService.extract(List.of()))
                .thenReturn(new CharacterExtractionResponse(List.of(
                        new CharacterDto("char_001", "林默", "年轻侦探", "冷静、敏锐", "调查真相", 1)
                )));

        mockMvc.perform(post("/api/screenplay/extract-characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapters\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.characters[0].id").value("char_001"))
                .andExpect(jsonPath("$.data.characters[0].name").value("林默"))
                .andExpect(jsonPath("$.data.characters[0].firstAppearedChapter").value(1));
    }

    @Test
    void planScenesReturnsSceneList() throws Exception {
        when(scenePlanningService.plan(List.of(), List.of()))
                .thenReturn(new ScenePlanningResponse(List.of(
                        new SceneDto(
                                "scene_001",
                                1,
                                "雨夜委托",
                                "林默事务所",
                                "夜晚",
                                List.of("char_001"),
                                "许岚委托林默调查失踪案。",
                                1
                        )
                )));

        mockMvc.perform(post("/api/screenplay/plan-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chapters\":[],\"characters\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.scenes[0].sceneId").value("scene_001"))
                .andExpect(jsonPath("$.data.scenes[0].sceneNumber").value(1))
                .andExpect(jsonPath("$.data.scenes[0].title").value("雨夜委托"))
                .andExpect(jsonPath("$.data.scenes[0].characters[0]").value("char_001"))
                .andExpect(jsonPath("$.data.scenes[0].sourceChapter").value(1));
    }

    @Test
    void generateYamlReturnsYamlText() throws Exception {
        when(yamlGenerationService.generate("雨夜来客", List.of(), List.of(), List.of()))
                .thenReturn(new ScreenplayYamlGenerationResponse("metadata:\n  title: 雨夜来客\n", 1, 2));

        mockMvc.perform(post("/api/screenplay/generate-yaml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"雨夜来客\",\"chapters\":[],\"characters\":[],\"scenes\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.yamlText").value("metadata:\n  title: 雨夜来客\n"))
                .andExpect(jsonPath("$.data.sceneCount").value(1))
                .andExpect(jsonPath("$.data.characterCount").value(2));
    }

    @Test
    void validateYamlReturnsValidationResult() throws Exception {
        when(yamlValidationService.validate("metadata:\n  title: Demo\n"))
                .thenReturn(new ScreenplayYamlValidationResponse(
                        false,
                        List.of("characters is required."),
                        List.of("production is missing; export will not include production notes."),
                        0,
                        0
                ));

        mockMvc.perform(post("/api/screenplay/validate-yaml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"yamlText\":\"metadata:\\n  title: Demo\\n\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.errors[0]").value("characters is required."))
                .andExpect(jsonPath("$.data.warnings[0]")
                        .value("production is missing; export will not include production notes."))
                .andExpect(jsonPath("$.data.sceneCount").value(0))
                .andExpect(jsonPath("$.data.characterCount").value(0));
    }

    @Test
    void validateYamlRejectsBlankYamlText() throws Exception {
        mockMvc.perform(post("/api/screenplay/validate-yaml")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"yamlText\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("YAML text must not be empty."));
    }

    @Test
    void generateFromTextReturnsFullWorkflowResult() throws Exception {
        when(screenplayGenerationWorkflowService.generate("Demo", "Chapter 1\\nA visitor arrives."))
                .thenReturn(new ScreenplayFullGenerationResponse(
                        List.of(),
                        List.of(),
                        List.of(),
                        "metadata:\n  title: Demo\n",
                        1,
                        2,
                        3
                ));

        mockMvc.perform(post("/api/screenplay/generate-from-text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Demo\",\"text\":\"Chapter 1\\\\nA visitor arrives.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.yamlText").value("metadata:\n  title: Demo\n"))
                .andExpect(jsonPath("$.data.chapterCount").value(1))
                .andExpect(jsonPath("$.data.characterCount").value(2))
                .andExpect(jsonPath("$.data.sceneCount").value(3));
    }

    @Test
    void generateFromTextRejectsBlankNovelText() throws Exception {
        mockMvc.perform(post("/api/screenplay/generate-from-text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Demo\",\"text\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Novel text must not be blank."));
    }
}
