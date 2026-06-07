package com.whatishope.screenplay.dto;

import java.util.List;

public record ScreenplayYamlValidationResponse(
        boolean valid,
        List<String> errors,
        int sceneCount,
        int characterCount
) {
}
