package com.whatishope.screenplay.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.common.GlobalExceptionHandler;
import com.whatishope.screenplay.dto.NovelUploadResponse;
import com.whatishope.screenplay.service.ChapterSplitService;
import com.whatishope.screenplay.service.NovelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(NovelController.class)
@Import(GlobalExceptionHandler.class)
class NovelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NovelService novelService;

    @MockBean
    private ChapterSplitService chapterSplitService;

    @Test
    void uploadReturnsNovelPreview() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample-novel.txt",
                "text/plain",
                "第一章 雨夜".getBytes()
        );
        when(novelService.upload(any(MultipartFile.class)))
                .thenReturn(new NovelUploadResponse("sample-novel.txt", 6, "第一章 雨夜"));

        mockMvc.perform(multipart("/api/novel/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.fileName").value("sample-novel.txt"))
                .andExpect(jsonPath("$.data.length").value(6))
                .andExpect(jsonPath("$.data.preview").value("第一章 雨夜"));
    }

    @Test
    void uploadReturnsBadRequestForInvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.pdf",
                "application/pdf",
                "content".getBytes()
        );
        when(novelService.upload(any(MultipartFile.class)))
                .thenThrow(new BadRequestException("Only .txt files are supported."));

        mockMvc.perform(multipart("/api/novel/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Only .txt files are supported."));
    }

    @Test
    void splitChaptersRejectsBlankText() throws Exception {
        mockMvc.perform(post("/api/novel/split-chapters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Novel text must not be blank."));
    }
}
