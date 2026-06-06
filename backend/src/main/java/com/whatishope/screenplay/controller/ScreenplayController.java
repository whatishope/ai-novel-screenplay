package com.whatishope.screenplay.controller;

import com.whatishope.screenplay.common.ApiResponse;
import com.whatishope.screenplay.dto.CharacterExtractionRequest;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.service.CharacterExtractionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/screenplay")
public class ScreenplayController {

    private final CharacterExtractionService characterExtractionService;

    public ScreenplayController(CharacterExtractionService characterExtractionService) {
        this.characterExtractionService = characterExtractionService;
    }

    @PostMapping("/extract-characters")
    public ApiResponse<CharacterExtractionResponse> extractCharacters(
            @RequestBody CharacterExtractionRequest request
    ) {
        return ApiResponse.ok(characterExtractionService.extract(request.chapters()));
    }
}
