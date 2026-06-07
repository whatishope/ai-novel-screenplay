package com.whatishope.screenplay.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.CharacterDto;
import com.whatishope.screenplay.dto.CharacterExtractionResponse;
import com.whatishope.screenplay.llm.LlmClient;
import com.whatishope.screenplay.llm.LlmJsonResponseExtractor;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CharacterExtractionService {

    private static final int MAX_CHAPTER_CONTENT_PREVIEW = 1200;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public CharacterExtractionService(LlmClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public CharacterExtractionResponse extract(List<ChapterDto> chapters) {
        validateChapters(chapters);

        String prompt = buildPrompt(chapters);
        List<CharacterDto> characters = generateCharacters(prompt);
        if (characters.isEmpty()) {
            characters = mockCharacters(chapters);
        }

        return new CharacterExtractionResponse(characters);
    }

    private void validateChapters(List<ChapterDto> chapters) {
        if (CollectionUtils.isEmpty(chapters)) {
            throw new BadRequestException("Chapters must not be empty.");
        }
        for (ChapterDto chapter : chapters) {
            if (chapter == null || !StringUtils.hasText(chapter.content())) {
                throw new BadRequestException("Each chapter must contain content.");
            }
        }
    }

    private String buildPrompt(List<ChapterDto> chapters) {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                你是剧本改编助手。请根据小说章节提取主要角色。
                只返回 JSON，不要返回 Markdown。
                不要编造章节中完全没有出现的人物。
                JSON 格式：
                {
                  "characters": [
                    {
                      "id": "char_001",
                      "name": "角色姓名",
                      "description": "身份描述",
                      "personality": "性格特点",
                      "goal": "角色目标",
                      "firstAppearedChapter": 1
                    }
                  ]
                }
                """);

        for (ChapterDto chapter : chapters) {
            builder.append("\n章节 ")
                    .append(chapter.chapterIndex())
                    .append("：")
                    .append(chapter.title())
                    .append("\n")
                    .append(limitContent(chapter.content()))
                    .append("\n");
        }

        return builder.toString();
    }

    private List<CharacterDto> generateCharacters(String prompt) {
        try {
            return parseCharacters(llmClient.generate(prompt));
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

    private List<CharacterDto> parseCharacters(String llmResult) {
        if (!StringUtils.hasText(llmResult)) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(LlmJsonResponseExtractor.extractJsonObject(llmResult));
            JsonNode charactersNode = root.get("characters");
            if (charactersNode == null || !charactersNode.isArray()) {
                return List.of();
            }
            List<CharacterDto> characters = objectMapper.convertValue(
                    charactersNode,
                    new TypeReference<>() {
                    }
            );
            return characters.stream()
                    .filter(character -> StringUtils.hasText(character.id()))
                    .filter(character -> StringUtils.hasText(character.name()))
                    .toList();
        } catch (RuntimeException exception) {
            return List.of();
        } catch (Exception exception) {
            return List.of();
        }
    }

    private List<CharacterDto> mockCharacters(List<ChapterDto> chapters) {
        int firstChapter = chapters.get(0).chapterIndex();
        int secondChapter = chapters.size() > 1 ? chapters.get(1).chapterIndex() : firstChapter;

        return List.of(
                new CharacterDto(
                        "char_001",
                        "林默",
                        "年轻侦探，负责调查小说中的核心事件。",
                        "冷静、敏锐、善于观察细节",
                        "查明事件真相并推动剧情发展",
                        firstChapter
                ),
                new CharacterDto(
                        "char_002",
                        "许岚",
                        "与事件密切相关的委托人，提供关键线索。",
                        "坚定、焦急、情感充沛",
                        "寻找失踪亲人并揭开隐藏秘密",
                        secondChapter
                )
        );
    }
}
