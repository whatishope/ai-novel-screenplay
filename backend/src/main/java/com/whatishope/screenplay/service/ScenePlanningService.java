package com.whatishope.screenplay.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.SceneDto;
import com.whatishope.screenplay.dto.ScenePlanningResponse;
import com.whatishope.screenplay.llm.LlmClient;
import com.whatishope.screenplay.llm.LlmJsonResponseExtractor;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class ScenePlanningService {

    private static final int MAX_CHAPTER_CONTENT_PREVIEW = 1000;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public ScenePlanningService(LlmClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public ScenePlanningResponse plan(List<ChapterDto> chapters, List<CharacterDto> characters) {
        validate(chapters, characters);

        String prompt = buildPrompt(chapters, characters);
        List<SceneDto> scenes = normalizeSceneNumbers(generateScenes(prompt));
        if (scenes.isEmpty()) {
            scenes = mockScenes(chapters, characters);
        }

        return new ScenePlanningResponse(scenes);
    }

    private void validate(List<ChapterDto> chapters, List<CharacterDto> characters) {
        if (CollectionUtils.isEmpty(chapters)) {
            throw new BadRequestException("Chapters must not be empty.");
        }
        if (CollectionUtils.isEmpty(characters)) {
            throw new BadRequestException("Characters must not be empty.");
        }
        for (ChapterDto chapter : chapters) {
            if (chapter == null || !StringUtils.hasText(chapter.content())) {
                throw new BadRequestException("Each chapter must contain content.");
            }
        }
        for (CharacterDto character : characters) {
            if (character == null || !StringUtils.hasText(character.id()) || !StringUtils.hasText(character.name())) {
                throw new BadRequestException("Each character must contain id and name.");
            }
        }
    }

    private String buildPrompt(List<ChapterDto> chapters, List<CharacterDto> characters) {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                你是剧本改编助手。请根据小说章节和角色列表规划剧本场景。
                只返回 JSON，不要返回 Markdown。
                场景必须引用角色列表中的角色 id，sourceChapter 必须来自输入章节。
                JSON 格式：
                {
                  "scenes": [
                    {
                      "sceneId": "scene_001",
                      "sceneNumber": 1,
                      "title": "场景标题",
                      "location": "地点",
                      "time": "时间",
                      "characters": ["char_001"],
                      "summary": "场景摘要",
                      "sourceChapter": 1
                    }
                  ]
                }
                场景编号必须从 1 开始连续递增。
                """);

        builder.append("\n角色列表：\n");
        for (CharacterDto character : characters) {
            builder.append("- ")
                    .append(character.id())
                    .append(" / ")
                    .append(character.name())
                    .append("：")
                    .append(character.description())
                    .append("\n");
        }

        builder.append("\n章节内容：\n");
        for (ChapterDto chapter : chapters) {
            builder.append("章节 ")
                    .append(chapter.chapterIndex())
                    .append("：")
                    .append(chapter.title())
                    .append("\n")
                    .append(limitContent(chapter.content()))
                    .append("\n");
        }

        return builder.toString();
    }

    private List<SceneDto> generateScenes(String prompt) {
        try {
            return parseScenes(llmClient.generate(prompt));
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private String limitContent(String content) {
        String stripped = content.strip();
        if (stripped.length() <= MAX_CHAPTER_CONTENT_PREVIEW) {
            return stripped;
        }
        return stripped.substring(0, MAX_CHAPTER_CONTENT_PREVIEW) + "...";
    }

    private List<SceneDto> parseScenes(String llmResult) {
        if (!StringUtils.hasText(llmResult)) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(LlmJsonResponseExtractor.extractJsonObject(llmResult));
            JsonNode scenesNode = root.get("scenes");
            if (scenesNode == null || !scenesNode.isArray()) {
                return List.of();
            }
            List<SceneDto> scenes = objectMapper.convertValue(
                    scenesNode,
                    new TypeReference<>() {
                    }
            );
            return scenes.stream()
                    .filter(scene -> StringUtils.hasText(scene.sceneId()))
                    .filter(scene -> StringUtils.hasText(scene.title()))
                    .filter(scene -> StringUtils.hasText(scene.summary()))
                    .toList();
        } catch (RuntimeException exception) {
            return List.of();
        } catch (Exception exception) {
            return List.of();
        }
    }

    private List<SceneDto> normalizeSceneNumbers(List<SceneDto> scenes) {
        return IntStream.range(0, scenes.size())
                .mapToObj(index -> {
                    SceneDto scene = scenes.get(index);
                    int sceneNumber = index + 1;
                    String sceneId = String.format("scene_%03d", sceneNumber);
                    return new SceneDto(
                            sceneId,
                            sceneNumber,
                            scene.title(),
                            scene.location(),
                            scene.time(),
                            scene.characters(),
                            scene.summary(),
                            scene.sourceChapter()
                    );
                })
                .toList();
    }

    private List<SceneDto> mockScenes(List<ChapterDto> chapters, List<CharacterDto> characters) {
        List<String> characterIds = characters.stream()
                .map(CharacterDto::id)
                .toList();

        return IntStream.range(0, chapters.size())
                .mapToObj(index -> {
                    ChapterDto chapter = chapters.get(index);
                    int sceneNumber = index + 1;
                    return new SceneDto(
                            String.format("scene_%03d", sceneNumber),
                            sceneNumber,
                            chapter.title(),
                            "待定地点",
                            "待定时间",
                            characterIds,
                            buildMockSummary(chapter),
                            chapter.chapterIndex()
                    );
                })
                .toList();
    }

    private String buildMockSummary(ChapterDto chapter) {
        String content = chapter.content().strip();
        String preview = content.length() > 80 ? content.substring(0, 80) + "..." : content;
        return "根据《" + chapter.title() + "》规划的剧本场景：" + preview;
    }
}
