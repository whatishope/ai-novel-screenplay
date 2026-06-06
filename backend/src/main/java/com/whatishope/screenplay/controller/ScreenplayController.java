package com.whatishope.screenplay.controller;

import com.whatishope.screenplay.common.ApiResponse;
import com.whatishope.screenplay.dto.CharacterExtractionRequest;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.dto.ScenePlanningRequest;
import com.whatishope.screenplay.dto.ScenePlanningResponse;
import com.whatishope.screenplay.service.CharacterExtractionService;
import com.whatishope.screenplay.service.ScenePlanningService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/screenplay")
public class ScreenplayController {

    private final CharacterExtractionService characterExtractionService;
    private final ScenePlanningService scenePlanningService;

    public ScreenplayController(
            CharacterExtractionService characterExtractionService,
            ScenePlanningService scenePlanningService
    ) {
        this.characterExtractionService = characterExtractionService;
        this.scenePlanningService = scenePlanningService;
    }

    @PostMapping("/extract-characters")
    public ApiResponse<CharacterExtractionResponse> extractCharacters(
            @RequestBody CharacterExtractionRequest request
    ) {
        return ApiResponse.ok(characterExtractionService.extract(request.chapters()));
    }

    @PostMapping("/plan-scenes")
    public ApiResponse<ScenePlanningResponse> planScenes(@RequestBody ScenePlanningRequest request) {
        return ApiResponse.ok(scenePlanningService.plan(request.chapters(), request.characters()));
    }
}
