package com.whatishope.screenplay.dto;

import jakarta.validation.constraints.NotBlank;

public record ChapterSplitRequest(
        @NotBlank(message = "Novel text must not be blank.")
        String text
) {
}
