package com.whatishope.screenplay.service;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.SceneDto;
import com.whatishope.screenplay.dto.ScreenplayYamlGenerationResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@Service
public class YamlGenerationService {

    public ScreenplayYamlGenerationResponse generate(
            String title,
            List<ChapterDto> chapters,
            List<CharacterDto> characters,
            List<SceneDto> scenes
    ) {
        validate(chapters, characters, scenes);

        Map<String, Object> screenplay = new LinkedHashMap<>();
        screenplay.put("metadata", buildMetadata(title, chapters));
        screenplay.put("characters", characters.stream().map(this::toCharacterMap).toList());
        screenplay.put("relationships", buildRelationships(characters, scenes));
        screenplay.put("scenes", scenes.stream().map(scene -> toSceneMap(scene, characters)).toList());
        screenplay.put("production", buildProduction());

        String yamlText = createYaml().dump(screenplay);
        return new ScreenplayYamlGenerationResponse(yamlText, scenes.size(), characters.size());
    }

    private void validate(List<ChapterDto> chapters, List<CharacterDto> characters, List<SceneDto> scenes) {
        if (CollectionUtils.isEmpty(chapters)) {
            throw new BadRequestException("Chapters must not be empty.");
        }
        if (CollectionUtils.isEmpty(characters)) {
            throw new BadRequestException("Characters must not be empty.");
        }
        if (CollectionUtils.isEmpty(scenes)) {
            throw new BadRequestException("Scenes must not be empty.");
        }
    }

    private Map<String, Object> buildMetadata(String title, List<ChapterDto> chapters) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("title", StringUtils.hasText(title) ? title.strip() : "AI 小说改编剧本");
        metadata.put("source_title", "用户上传小说");
        metadata.put("language", "zh-CN");
        metadata.put("genre", "待定");
        metadata.put("style", "短剧");
        metadata.put("chapter_count", chapters.size());
        metadata.put("generated_at", OffsetDateTime.now().toString());
        metadata.put("version", "1.0");
        return metadata;
    }

    private Map<String, Object> toCharacterMap(CharacterDto character) {
        Map<String, Object> characterMap = new LinkedHashMap<>();
        characterMap.put("id", character.id());
        characterMap.put("name", character.name());
        characterMap.put("description", character.description());
        characterMap.put("personality", character.personality());
        characterMap.put("goal", character.goal());
        characterMap.put("first_appeared_chapter", character.firstAppearedChapter());
        return characterMap;
    }

    private List<Map<String, Object>> buildRelationships(List<CharacterDto> characters, List<SceneDto> scenes) {
        Map<String, CharacterDto> characterById = new LinkedHashMap<>();
        for (CharacterDto character : characters) {
            characterById.put(character.id(), character);
        }

        List<Map<String, Object>> relationships = new ArrayList<>();
        Set<String> relationshipKeys = new HashSet<>();
        for (SceneDto scene : scenes) {
            List<String> sceneCharacterIds = scene.characters() == null
                    ? List.of()
                    : scene.characters().stream()
                            .filter(characterById::containsKey)
                            .distinct()
                            .toList();

            for (int i = 0; i < sceneCharacterIds.size(); i++) {
                for (int j = i + 1; j < sceneCharacterIds.size(); j++) {
                    String from = sceneCharacterIds.get(i);
                    String to = sceneCharacterIds.get(j);
                    String relationshipKey = from.compareTo(to) < 0 ? from + "->" + to : to + "->" + from;
                    if (relationshipKeys.add(relationshipKey)) {
                        relationships.add(toRelationshipMap(from, to, scene, characterById));
                    }
                }
            }
        }
        return relationships;
    }

    private Map<String, Object> toRelationshipMap(
            String from,
            String to,
            SceneDto scene,
            Map<String, CharacterDto> characterById
    ) {
        Map<String, Object> relationshipMap = new LinkedHashMap<>();
        relationshipMap.put("from", from);
        relationshipMap.put("to", to);
        relationshipMap.put("type", "共现");
        relationshipMap.put(
                "description",
                characterById.get(from).name() + " 与 " + characterById.get(to).name()
                        + " 在《" + scene.title() + "》中共同出场。"
        );
        return relationshipMap;
    }

    private Map<String, Object> toSceneMap(SceneDto scene, List<CharacterDto> characters) {
        Map<String, Object> sceneMap = new LinkedHashMap<>();
        sceneMap.put("scene_id", scene.sceneId());
        sceneMap.put("scene_number", scene.sceneNumber());
        sceneMap.put("title", scene.title());
        sceneMap.put("location", scene.location());
        sceneMap.put("time", scene.time());
        sceneMap.put("characters", scene.characters());
        sceneMap.put("summary", scene.summary());
        sceneMap.put("conflict", "待补充");
        sceneMap.put("actions", buildMockActions(scene));
        sceneMap.put("dialogues", buildMockDialogues(scene, characters));
        sceneMap.put("source_trace", buildSourceTrace(scene));
        return sceneMap;
    }

    private List<Map<String, Object>> buildMockActions(SceneDto scene) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("order", 1);
        action.put("content", scene.summary());
        action.put("source_trace", buildSourceTrace(scene));
        return List.of(action);
    }

    private List<Map<String, Object>> buildMockDialogues(SceneDto scene, List<CharacterDto> characters) {
        String characterId = scene.characters() == null || scene.characters().isEmpty()
                ? characters.get(0).id()
                : scene.characters().get(0);
        String characterName = characters.stream()
                .filter(character -> character.id().equals(characterId))
                .map(CharacterDto::name)
                .findFirst()
                .orElse(characterId);

        Map<String, Object> dialogue = new LinkedHashMap<>();
        dialogue.put("order", 1);
        dialogue.put("character", characterId);
        dialogue.put("character_name", characterName);
        dialogue.put("content", "这里需要根据原文进一步打磨对白。");
        dialogue.put("tone", "自然");
        dialogue.put("source_trace", buildSourceTrace(scene));
        return List.of(dialogue);
    }

    private Map<String, Object> buildSourceTrace(SceneDto scene) {
        Map<String, Object> sourceTrace = new LinkedHashMap<>();
        sourceTrace.put("chapter_index", scene.sourceChapter());
        sourceTrace.put("chapter_title", "来源章节 " + scene.sourceChapter());
        sourceTrace.put("paragraph_range", "待补充");
        sourceTrace.put("note", "根据场景规划生成。");
        return sourceTrace;
    }

    private Map<String, Object> buildProduction() {
        Map<String, Object> production = new LinkedHashMap<>();
        production.put("estimated_duration", "待定");
        production.put("target_format", "短剧");
        production.put("notes", List.of("本 YAML 为结构化剧本初稿，可继续编辑和校验。"));
        return production;
    }

    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setAllowUnicode(true);
        options.setIndent(2);
        return new Yaml(options);
    }
}
