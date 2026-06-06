package com.whatishope.screenplay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.NovelUploadResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class NovelServiceTest {

    private final NovelService novelService = new NovelService();

    @Test
    void uploadReadsUtf8TxtAndReturnsPreview() {
        String text = "第一章 雨夜\n林默听见敲门声。";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample-novel.txt",
                "text/plain",
                text.getBytes(StandardCharsets.UTF_8)
        );

        NovelUploadResponse response = novelService.upload(file);

        assertThat(response.fileName()).isEqualTo("sample-novel.txt");
        assertThat(response.length()).isEqualTo(text.length());
        assertThat(response.preview()).isEqualTo(text);
    }

    @Test
    void uploadLimitsPreviewToFirstFiveHundredCharacters() {
        String text = "一".repeat(600);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "long.txt",
                "text/plain",
                text.getBytes(StandardCharsets.UTF_8)
        );

        NovelUploadResponse response = novelService.upload(file);

        assertThat(response.length()).isEqualTo(600);
        assertThat(response.preview()).hasSize(500);
    }

    @Test
    void uploadRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThatThrownBy(() -> novelService.upload(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Uploaded file must not be empty.");
    }

    @Test
    void uploadRejectsNonTxtFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.md",
                "text/markdown",
                "content".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> novelService.upload(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only .txt files are supported.");
    }

    @Test
    void uploadRejectsFileLargerThanFiveMb() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.txt",
                "text/plain",
                new byte[5 * 1024 * 1024 + 1]
        );

        assertThatThrownBy(() -> novelService.upload(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Uploaded file size must not exceed 5MB.");
    }

    @Test
    void uploadRejectsInvalidUtf8Text() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "broken.txt",
                "text/plain",
                new byte[]{(byte) 0xC3, 0x28}
        );

        assertThatThrownBy(() -> novelService.upload(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Uploaded file must be valid UTF-8 text.");
    }
}
