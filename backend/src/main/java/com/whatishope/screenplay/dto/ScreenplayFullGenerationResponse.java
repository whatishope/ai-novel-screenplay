package com.whatishope.screenplay.dto;

import java.util.List;

public record ScreenplayFullGenerationResponse(
        List<ChapterDto> chapters,
        List<CharacterDto> characters,
        List<SceneDto> scenes,
        String yamlText,
        int chapterCount,
        int characterCount,
        int sceneCount
) {
}
