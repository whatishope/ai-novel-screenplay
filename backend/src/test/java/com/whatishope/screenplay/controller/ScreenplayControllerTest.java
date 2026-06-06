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
import com.whatishope.screenplay.service.CharacterExtractionService;
import com.whatishope.screenplay.service.ScenePlanningService;
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
}
