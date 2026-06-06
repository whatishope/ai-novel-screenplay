package com.whatishope.screenplay.dto;

import java.util.List;

public record SceneDto(
        String sceneId,
        int sceneNumber,
        String title,
        String location,
        String time,
        List<String> characters,
        String summary,
        int sourceChapter
) {
}
