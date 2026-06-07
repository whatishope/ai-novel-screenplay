package com.whatishope.screenplay.dto;

import jakarta.validation.constraints.NotBlank;

public record ScreenplayFullGenerationRequest(
        String title,
        @NotBlank(message = "Novel text must not be blank.")
        String text
) {
}
