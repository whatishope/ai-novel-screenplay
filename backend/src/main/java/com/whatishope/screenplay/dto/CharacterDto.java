package com.whatishope.screenplay.dto;

public record CharacterDto(
        String id,
        String name,
        String description,
        String personality,
        String goal,
        int firstAppearedChapter
) {
}
