package com.whatishope.screenplay.service;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ScreenplayYamlValidationResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

@Service
public class YamlValidationService {

    public ScreenplayYamlValidationResponse validate(String yamlText) {
        if (!StringUtils.hasText(yamlText)) {
            throw new BadRequestException("YAML text must not be empty.");
        }

        Object parsed;
        try {
            parsed = new Yaml().load(yamlText);
        } catch (YAMLException exception) {
            return invalid(List.of("YAML syntax error: " + exception.getMessage()));
        }

        if (!(parsed instanceof Map<?, ?> root)) {
            return invalid(List.of("Root document must be an object."));
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        validateRoot(root, errors);
        Map<?, ?> metadata = requireMap(root, "metadata", errors);
        validateMetadata(metadata, errors);

        List<?> characters = requireList(root, "characters", errors);
        Set<String> characterIds = validateCharacters(characters, errors);

        List<?> scenes = requireList(root, "scenes", errors);
        validateScenes(scenes, characterIds, errors);

        validateRelationships(root, characterIds, errors);
        validateOptionalMap(root, "production", errors);
        collectWarnings(root, scenes, warnings);

        return new ScreenplayYamlValidationResponse(
                errors.isEmpty(),
                List.copyOf(errors),
                List.copyOf(warnings),
                scenes == null ? 0 : scenes.size(),
                characters == null ? 0 : characters.size(),
                countRelationships(root)
        );
    }

    private ScreenplayYamlValidationResponse invalid(List<String> errors) {
        return new ScreenplayYamlValidationResponse(false, errors, List.of(), 0, 0, 0);
    }

    private void validateRoot(Map<?, ?> root, List<String> errors) {
        requirePresent(root, "metadata", errors);
        requirePresent(root, "characters", errors);
        requirePresent(root, "scenes", errors);
    }

    private void validateMetadata(Map<?, ?> metadata, List<String> errors) {
        if (metadata == null) {
            return;
        }
        requireText(metadata, "metadata.title", "title", errors);
        requireText(metadata, "metadata.language", "language", errors);
        requireNumber(metadata, "metadata.chapter_count", "chapter_count", errors);
        requireText(metadata, "metadata.version", "version", errors);
    }

    private Set<String> validateCharacters(List<?> characters, List<String> errors) {
        Set<String> characterIds = new HashSet<>();
        if (characters == null) {
            return characterIds;
        }
        if (characters.isEmpty()) {
            errors.add("characters must not be empty.");
            return characterIds;
        }

        for (int i = 0; i < characters.size(); i++) {
            String path = "characters[" + i + "]";
            Object item = characters.get(i);
            if (!(item instanceof Map<?, ?> character)) {
                errors.add(path + " must be an object.");
                continue;
            }

            String id = requireText(character, path + ".id", "id", errors);
            requireText(character, path + ".name", "name", errors);
            if (StringUtils.hasText(id) && !characterIds.add(id)) {
                errors.add(path + ".id duplicates character id '" + id + "'.");
            }
        }
        return characterIds;
    }

    private void validateScenes(List<?> scenes, Set<String> characterIds, List<String> errors) {
        if (scenes == null) {
            return;
        }
        if (scenes.isEmpty()) {
            errors.add("scenes must not be empty.");
            return;
        }

        Set<String> sceneIds = new HashSet<>();
        for (int i = 0; i < scenes.size(); i++) {
            String path = "scenes[" + i + "]";
            Object item = scenes.get(i);
            if (!(item instanceof Map<?, ?> scene)) {
                errors.add(path + " must be an object.");
                continue;
            }

            String sceneId = requireText(scene, path + ".scene_id", "scene_id", errors);
            if (StringUtils.hasText(sceneId) && !sceneIds.add(sceneId)) {
                errors.add(path + ".scene_id duplicates scene id '" + sceneId + "'.");
            }
            Number sceneNumber = requireNumber(scene, path + ".scene_number", "scene_number", errors);
            if (sceneNumber != null && sceneNumber.intValue() != i + 1) {
                errors.add(path + ".scene_number must be " + (i + 1) + ".");
            }
            requireText(scene, path + ".title", "title", errors);
            requireText(scene, path + ".summary", "summary", errors);
            validateCharacterReferences(scene, path, characterIds, errors);
            validateActions(scene, path, errors);
            validateDialogues(scene, path, characterIds, errors);
            validateSourceTrace(scene, path + ".source_trace", errors);
        }
    }

    private void validateCharacterReferences(
            Map<?, ?> scene,
            String path,
            Set<String> characterIds,
            List<String> errors
    ) {
        if (!scene.containsKey("characters")) {
            return;
        }
        Object value = scene.get("characters");
        if (!(value instanceof List<?> references)) {
            errors.add(path + ".characters must be an array.");
            return;
        }
        for (int i = 0; i < references.size(); i++) {
            Object reference = references.get(i);
            if (!(reference instanceof String characterId) || !StringUtils.hasText(characterId)) {
                errors.add(path + ".characters[" + i + "] must be a non-empty string.");
                continue;
            }
            if (!characterIds.contains(characterId)) {
                errors.add(path + ".characters[" + i + "] references unknown character '" + characterId + "'.");
            }
        }
    }

    private void validateActions(Map<?, ?> scene, String path, List<String> errors) {
        if (!scene.containsKey("actions")) {
            return;
        }
        Object value = scene.get("actions");
        if (!(value instanceof List<?> actions)) {
            errors.add(path + ".actions must be an array.");
            return;
        }
        for (int i = 0; i < actions.size(); i++) {
            String actionPath = path + ".actions[" + i + "]";
            Object actionItem = actions.get(i);
            if (!(actionItem instanceof Map<?, ?> action)) {
                errors.add(actionPath + " must be an object.");
                continue;
            }
            requireNumber(action, actionPath + ".order", "order", errors);
            requireText(action, actionPath + ".content", "content", errors);
            validateSourceTrace(action, actionPath + ".source_trace", errors);
        }
    }

    private void validateDialogues(
            Map<?, ?> scene,
            String path,
            Set<String> characterIds,
            List<String> errors
    ) {
        if (!scene.containsKey("dialogues")) {
            return;
        }
        Object value = scene.get("dialogues");
        if (!(value instanceof List<?> dialogues)) {
            errors.add(path + ".dialogues must be an array.");
            return;
        }
        for (int i = 0; i < dialogues.size(); i++) {
            String dialoguePath = path + ".dialogues[" + i + "]";
            Object dialogueItem = dialogues.get(i);
            if (!(dialogueItem instanceof Map<?, ?> dialogue)) {
                errors.add(dialoguePath + " must be an object.");
                continue;
            }
            requireNumber(dialogue, dialoguePath + ".order", "order", errors);
            String characterId = requireText(dialogue, dialoguePath + ".character", "character", errors);
            if (StringUtils.hasText(characterId) && !characterIds.contains(characterId)) {
                errors.add(dialoguePath + ".character references unknown character '" + characterId + "'.");
            }
            requireText(dialogue, dialoguePath + ".content", "content", errors);
            validateSourceTrace(dialogue, dialoguePath + ".source_trace", errors);
        }
    }

    private void validateSourceTrace(Map<?, ?> owner, String path, List<String> errors) {
        if (!owner.containsKey("source_trace")) {
            return;
        }
        Object value = owner.get("source_trace");
        if (!(value instanceof Map<?, ?> sourceTrace)) {
            errors.add(path + " must be an object.");
            return;
        }
        requireNumber(sourceTrace, path + ".chapter_index", "chapter_index", errors);
    }

    private void validateRelationships(Map<?, ?> root, Set<String> characterIds, List<String> errors) {
        if (!root.containsKey("relationships")) {
            return;
        }
        Object value = root.get("relationships");
        if (!(value instanceof List<?> relationships)) {
            errors.add("relationships must be an array.");
            return;
        }

        for (int i = 0; i < relationships.size(); i++) {
            String path = "relationships[" + i + "]";
            Object item = relationships.get(i);
            if (!(item instanceof Map<?, ?> relationship)) {
                errors.add(path + " must be an object.");
                continue;
            }

            String from = requireText(relationship, path + ".from", "from", errors);
            String to = requireText(relationship, path + ".to", "to", errors);
            requireText(relationship, path + ".type", "type", errors);
            validateRelationshipReference(from, path + ".from", characterIds, errors);
            validateRelationshipReference(to, path + ".to", characterIds, errors);
        }
    }

    private void validateOptionalMap(Map<?, ?> root, String key, List<String> errors) {
        if (root.containsKey(key) && !(root.get(key) instanceof Map<?, ?>)) {
            errors.add(key + " must be an object.");
        }
    }

    private int countRelationships(Map<?, ?> root) {
        Object relationships = root.get("relationships");
        return relationships instanceof List<?> list ? list.size() : 0;
    }

    private void collectWarnings(Map<?, ?> root, List<?> scenes, List<String> warnings) {
        collectRelationshipWarnings(root, warnings);
        collectProductionWarnings(root, warnings);
        collectSceneWarnings(scenes, warnings);
    }

    private void collectRelationshipWarnings(Map<?, ?> root, List<String> warnings) {
        if (!root.containsKey("relationships")) {
            warnings.add("relationships is missing; character relationship graph will be empty.");
            return;
        }
        Object relationships = root.get("relationships");
        if (relationships instanceof List<?> list && list.isEmpty()) {
            warnings.add("relationships is empty; character relationship graph will be empty.");
        }
    }

    private void collectProductionWarnings(Map<?, ?> root, List<String> warnings) {
        if (!root.containsKey("production")) {
            warnings.add("production is missing; export will not include production notes.");
        }
    }

    private void collectSceneWarnings(List<?> scenes, List<String> warnings) {
        if (scenes == null) {
            return;
        }
        for (int i = 0; i < scenes.size(); i++) {
            Object item = scenes.get(i);
            if (!(item instanceof Map<?, ?> scene)) {
                continue;
            }
            String path = "scenes[" + i + "]";
            warnIfMissingTraceableBlock(scene, path, "source_trace", warnings);
            warnIfMissingTraceableBlock(scene, path, "actions", warnings);
            warnIfMissingTraceableBlock(scene, path, "dialogues", warnings);
        }
    }

    private void warnIfMissingTraceableBlock(
            Map<?, ?> scene,
            String path,
            String key,
            List<String> warnings
    ) {
        if (!scene.containsKey(key)) {
            warnings.add(path + "." + key + " is missing; generated screenplay may be less traceable.");
            return;
        }
        Object value = scene.get(key);
        if (value instanceof List<?> list && list.isEmpty()) {
            warnings.add(path + "." + key + " is empty; generated screenplay may be less traceable.");
        }
    }

    private void validateRelationshipReference(
            String characterId,
            String path,
            Set<String> characterIds,
            List<String> errors
    ) {
        if (StringUtils.hasText(characterId) && !characterIds.contains(characterId)) {
            errors.add(path + " references unknown character '" + characterId + "'.");
        }
    }

    private void requirePresent(Map<?, ?> owner, String key, List<String> errors) {
        if (!owner.containsKey(key)) {
            errors.add(key + " is required.");
        }
    }

    private Map<?, ?> requireMap(Map<?, ?> owner, String key, List<String> errors) {
        Object value = owner.get(key);
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        if (owner.containsKey(key)) {
            errors.add(key + " must be an object.");
        }
        return null;
    }

    private List<?> requireList(Map<?, ?> owner, String key, List<String> errors) {
        Object value = owner.get(key);
        if (value instanceof List<?> list) {
            return list;
        }
        if (owner.containsKey(key)) {
            errors.add(key + " must be an array.");
        }
        return null;
    }

    private String requireText(Map<?, ?> owner, String path, String key, List<String> errors) {
        Object value = owner.get(key);
        if (value instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        errors.add(path + " is required and must be a non-empty string.");
        return null;
    }

    private Number requireNumber(Map<?, ?> owner, String path, String key, List<String> errors) {
        Object value = owner.get(key);
        if (value instanceof Number number) {
            return number;
        }
        errors.add(path + " is required and must be a number.");
        return null;
    }
}
