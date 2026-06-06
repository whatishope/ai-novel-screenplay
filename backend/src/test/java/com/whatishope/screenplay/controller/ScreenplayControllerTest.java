package com.whatishope.screenplay.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.whatishope.screenplay.common.GlobalExceptionHandler;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.service.CharacterExtractionService;
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
}
