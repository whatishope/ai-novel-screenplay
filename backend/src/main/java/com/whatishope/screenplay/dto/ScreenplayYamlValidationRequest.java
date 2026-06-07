package com.whatishope.screenplay.dto;

import jakarta.validation.constraints.NotBlank;

public record ScreenplayYamlValidationRequest(
        @NotBlank(message = "YAML text must not be empty.")
        String yamlText
) {
}
