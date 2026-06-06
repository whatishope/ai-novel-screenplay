package com.whatishope.screenplay.dto;

import java.util.List;

public record ScenePlanningRequest(List<ChapterDto> chapters, List<CharacterDto> characters) {
}
