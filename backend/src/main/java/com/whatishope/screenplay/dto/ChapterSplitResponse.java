package com.whatishope.screenplay.dto;

import java.util.List;

public record ChapterSplitResponse(int chapterCount, List<ChapterDto> chapters) {
}
