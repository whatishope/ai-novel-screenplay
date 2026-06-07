package com.whatishope.screenplay.controller;

import com.whatishope.screenplay.common.ApiResponse;
import com.whatishope.screenplay.dto.CharacterExtractionRequest;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.dto.ScenePlanningRequest;
import com.whatishope.screenplay.dto.ScenePlanningResponse;
import com.whatishope.screenplay.dto.ScreenplayFullGenerationRequest;
import com.whatishope.screenplay.dto.ScreenplayFullGenerationResponse;
import com.whatishope.screenplay.dto.ScreenplayYamlGenerationRequest;
import com.whatishope.screenplay.dto.ScreenplayYamlGenerationResponse;
import com.whatishope.screenplay.dto.ScreenplayYamlValidationRequest;
import com.whatishope.screenplay.dto.ScreenplayYamlValidationResponse;
import com.whatishope.screenplay.service.CharacterExtractionService;
import com.whatishope.screenplay.service.ScenePlanningService;
import com.whatishope.screenplay.service.ScreenplayGenerationWorkflowService;
import com.whatishope.screenplay.service.YamlGenerationService;
import com.whatishope.screenplay.service.YamlValidationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/screenplay")
public class ScreenplayController {

    private final CharacterExtractionService characterExtractionService;
    private final ScenePlanningService scenePlanningService;
    private final YamlGenerationService yamlGenerationService;
    private final YamlValidationService yamlValidationService;
    private final ScreenplayGenerationWorkflowService screenplayGenerationWorkflowService;

    public ScreenplayController(
            CharacterExtractionService characterExtractionService,
            ScenePlanningService scenePlanningService,
            YamlGenerationService yamlGenerationService,
            YamlValidationService yamlValidationService,
            ScreenplayGenerationWorkflowService screenplayGenerationWorkflowService
    ) {
        this.characterExtractionService = characterExtractionService;
        this.scenePlanningService = scenePlanningService;
        this.yamlGenerationService = yamlGenerationService;
        this.yamlValidationService = yamlValidationService;
        this.screenplayGenerationWorkflowService = screenplayGenerationWorkflowService;
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

    @PostMapping("/generate-yaml")
    public ApiResponse<ScreenplayYamlGenerationResponse> generateYaml(
            @RequestBody ScreenplayYamlGenerationRequest request
    ) {
        return ApiResponse.ok(yamlGenerationService.generate(
                request.title(),
                request.chapters(),
                request.characters(),
                request.scenes()
        ));
    }

    @PostMapping("/validate-yaml")
    public ApiResponse<ScreenplayYamlValidationResponse> validateYaml(
            @Valid @RequestBody ScreenplayYamlValidationRequest request
    ) {
        return ApiResponse.ok(yamlValidationService.validate(request.yamlText()));
    }

    @PostMapping("/generate-from-text")
    public ApiResponse<ScreenplayFullGenerationResponse> generateFromText(
            @Valid @RequestBody ScreenplayFullGenerationRequest request
    ) {
        return ApiResponse.ok(screenplayGenerationWorkflowService.generate(request.title(), request.text()));
    }
}
