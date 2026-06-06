package com.whatishope.screenplay.controller;

import com.whatishope.screenplay.common.ApiResponse;
import com.whatishope.screenplay.dto.ChapterSplitRequest;
import com.whatishope.screenplay.dto.ChapterSplitResponse;
import com.whatishope.screenplay.dto.NovelUploadResponse;
import com.whatishope.screenplay.service.ChapterSplitService;
import com.whatishope.screenplay.service.NovelService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/novel")
public class NovelController {

    private final NovelService novelService;
    private final ChapterSplitService chapterSplitService;

    public NovelController(NovelService novelService, ChapterSplitService chapterSplitService) {
        this.novelService = novelService;
        this.chapterSplitService = chapterSplitService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<NovelUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(novelService.upload(file));
    }

    @PostMapping("/split-chapters")
    public ApiResponse<ChapterSplitResponse> splitChapters(@RequestBody ChapterSplitRequest request) {
        return ApiResponse.ok(chapterSplitService.split(request.text()));
    }
}
