package com.whatishope.screenplay.dto;

import java.util.List;

public record ScreenplayYamlGenerationRequest(
        String title,
        List<ChapterDto> chapters,
        List<CharacterDto> characters,
        List<SceneDto> scenes
) {
}
