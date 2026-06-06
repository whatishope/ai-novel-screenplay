package com.whatishope.screenplay.controller;

import com.whatishope.screenplay.common.ApiResponse;
import com.whatishope.screenplay.dto.NovelUploadResponse;
import com.whatishope.screenplay.service.NovelService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/novel")
public class NovelController {

    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<NovelUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(novelService.upload(file));
    }
}
