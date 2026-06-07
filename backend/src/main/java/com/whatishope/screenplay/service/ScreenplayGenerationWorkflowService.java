package com.whatishope.screenplay.service;

import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.SceneDto;
import com.whatishope.screenplay.dto.ScreenplayFullGenerationResponse;
import com.whatishope.screenplay.dto.ScreenplayYamlGenerationResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScreenplayGenerationWorkflowService {

    private final ChapterSplitService chapterSplitService;
    private final CharacterExtractionService characterExtractionService;
    private final ScenePlanningService scenePlanningService;
    private final YamlGenerationService yamlGenerationService;

    public ScreenplayGenerationWorkflowService(
            ChapterSplitService chapterSplitService,
            CharacterExtractionService characterExtractionService,
            ScenePlanningService scenePlanningService,
            YamlGenerationService yamlGenerationService
    ) {
        this.chapterSplitService = chapterSplitService;
        this.characterExtractionService = characterExtractionService;
        this.scenePlanningService = scenePlanningService;
        this.yamlGenerationService = yamlGenerationService;
    }

    public ScreenplayFullGenerationResponse generate(String title, String text) {
        List<ChapterDto> chapters = chapterSplitService.split(text).chapters();
        List<CharacterDto> characters = characterExtractionService.extract(chapters).characters();
        List<SceneDto> scenes = scenePlanningService.plan(chapters, characters).scenes();
        ScreenplayYamlGenerationResponse yaml = yamlGenerationService.generate(title, chapters, characters, scenes);

        return new ScreenplayFullGenerationResponse(
                chapters,
                characters,
                scenes,
                yaml.yamlText(),
                chapters.size(),
                characters.size(),
                scenes.size()
        );
    }
}
