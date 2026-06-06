package com.whatishope.screenplay.service;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.ChapterDto;
import com.whatishope.screenplay.dto.ChapterSplitResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChapterSplitService {

    private static final int FALLBACK_CHAPTER_COUNT = 3;
    private static final Pattern CHAPTER_TITLE_PATTERN = Pattern.compile(
            "(?im)^\\s*((?:第[0-9一二三四五六七八九十百千万零〇两]+[章节回]|Chapter\\s+\\d+)[^\\r\\n]*)\\s*(?:\\r?\\n|$)"
    );

    public ChapterSplitResponse split(String text) {
        if (!StringUtils.hasText(text)) {
            throw new BadRequestException("Novel text must not be blank.");
        }

        String normalizedText = normalizeLineBreaks(text).strip();
        List<ChapterDto> chapters = splitByChapterTitle(normalizedText);
        if (chapters.isEmpty()) {
            chapters = splitEvenly(normalizedText);
        }

        return new ChapterSplitResponse(chapters.size(), chapters);
    }

    private List<ChapterDto> splitByChapterTitle(String text) {
        Matcher matcher = CHAPTER_TITLE_PATTERN.matcher(text);
        List<ChapterMatch> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(new ChapterMatch(matcher.group(1).trim(), matcher.start(), matcher.end()));
        }

        if (matches.size() < 2) {
            return List.of();
        }

        List<ChapterDto> chapters = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            ChapterMatch current = matches.get(i);
            int contentEnd = i + 1 < matches.size() ? matches.get(i + 1).titleStart() : text.length();
            String content = text.substring(current.contentStart(), contentEnd).strip();
            chapters.add(toChapterDto(i + 1, current.title(), content));
        }
        return chapters;
    }

    private List<ChapterDto> splitEvenly(String text) {
        List<ChapterDto> chapters = new ArrayList<>();
        int length = text.length();
        int start = 0;

        for (int i = 1; i <= FALLBACK_CHAPTER_COUNT; i++) {
            int end = i == FALLBACK_CHAPTER_COUNT ? length : Math.round(length * i / (float) FALLBACK_CHAPTER_COUNT);
            String content = text.substring(start, end).strip();
            chapters.add(toChapterDto(i, "自动切分 " + i, content));
            start = end;
        }

        return chapters;
    }

    private ChapterDto toChapterDto(int chapterIndex, String title, String content) {
        return new ChapterDto(chapterIndex, title, content, countWords(content));
    }

    private int countWords(String text) {
        return (int) text.codePoints()
                .filter(codePoint -> !Character.isWhitespace(codePoint))
                .count();
    }

    private String normalizeLineBreaks(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    private record ChapterMatch(String title, int titleStart, int contentStart) {
    }
}
