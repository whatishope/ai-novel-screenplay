package com.whatishope.screenplay.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.whatishope.screenplay.common.GlobalExceptionHandler;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.ChapterSplitResponse;
import com.whatishope.screenplay.service.ChapterSplitService;
import com.whatishope.screenplay.service.NovelService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NovelController.class)
@Import(GlobalExceptionHandler.class)
class ChapterSplitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NovelService novelService;

    @MockBean
    private ChapterSplitService chapterSplitService;

    @Test
    void splitChaptersReturnsChapterList() throws Exception {
        when(chapterSplitService.split("第一章 雨夜\n内容"))
                .thenReturn(new ChapterSplitResponse(1, List.of(
                        new ChapterDto(1, "第一章 雨夜", "内容", 2)
                )));

        mockMvc.perform(post("/api/novel/split-chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"第一章 雨夜\\n内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.chapterCount").value(1))
                .andExpect(jsonPath("$.data.chapters[0].chapterIndex").value(1))
                .andExpect(jsonPath("$.data.chapters[0].title").value("第一章 雨夜"))
                .andExpect(jsonPath("$.data.chapters[0].content").value("内容"))
                .andExpect(jsonPath("$.data.chapters[0].wordCount").value(2));
    }
}
