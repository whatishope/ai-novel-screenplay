package com.whatishope.screenplay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterSplitResponse;
import org.junit.jupiter.api.Test;

class ChapterSplitServiceTest {

    private final ChapterSplitService chapterSplitService = new ChapterSplitService();

    @Test
    void splitRecognizesChineseChapterTitles() {
        String text = """
                第一章 雨夜
                林默听见敲门声。
                第二章 来客
                许岚递来一把旧钥匙。
                第三回 暗门
                货架后露出地下通道。
                """;

        ChapterSplitResponse response = chapterSplitService.split(text);

        assertThat(response.chapterCount()).isEqualTo(3);
        assertThat(response.chapters()).extracting("chapterIndex").containsExactly(1, 2, 3);
        assertThat(response.chapters()).extracting("title")
                .containsExactly("第一章 雨夜", "第二章 来客", "第三回 暗门");
        assertThat(response.chapters().get(0).content()).isEqualTo("林默听见敲门声。");
        assertThat(response.chapters().get(0).wordCount()).isEqualTo("林默听见敲门声。".length());
    }

    @Test
    void splitRecognizesArabicAndEnglishChapterTitles() {
        String text = """
                第1章 开端
                第一段内容。
                Chapter 2 Warehouse
                第二段内容。
                """;

        ChapterSplitResponse response = chapterSplitService.split(text);

        assertThat(response.chapterCount()).isEqualTo(2);
        assertThat(response.chapters()).extracting("title")
                .containsExactly("第1章 开端", "Chapter 2 Warehouse");
    }

    @Test
    void splitFallsBackToThreePartsWhenNoChapterTitleExists() {
        ChapterSplitResponse response = chapterSplitService.split("一二三四五六七八九");

        assertThat(response.chapterCount()).isEqualTo(3);
        assertThat(response.chapters()).extracting("chapterIndex").containsExactly(1, 2, 3);
        assertThat(response.chapters()).extracting("title")
                .containsExactly("自动切分 1", "自动切分 2", "自动切分 3");
        assertThat(response.chapters()).extracting("content")
                .containsExactly("一二三", "四五六", "七八九");
    }

    @Test
    void splitRejectsBlankText() {
        assertThatThrownBy(() -> chapterSplitService.split("  "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Novel text must not be blank.");
    }
}
