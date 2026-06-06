package com.whatishope.screenplay.service;

import com.whatishope.screenplay.common.BadRequestException;
import com.whatishope.screenplay.dto.NovelUploadResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NovelService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final int PREVIEW_LENGTH = 500;

    public NovelUploadResponse upload(MultipartFile file) {
        validateFile(file);

        String fileName = normalizeFileName(file.getOriginalFilename());
        String text = readUtf8Text(file);
        String preview = text.substring(0, Math.min(PREVIEW_LENGTH, text.length()));

        return new NovelUploadResponse(fileName, text.length(), preview);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file must not be empty.");
        }

        String fileName = normalizeFileName(file.getOriginalFilename());
        if (!StringUtils.hasText(fileName) || !fileName.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            throw new BadRequestException("Only .txt files are supported.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Uploaded file size must not exceed 5MB.");
        }
    }

    private String normalizeFileName(String originalFileName) {
        String cleanedFileName = StringUtils.cleanPath(originalFileName == null ? "" : originalFileName)
                .replace("\\", "/");
        int slashIndex = cleanedFileName.lastIndexOf('/');
        if (slashIndex >= 0) {
            return cleanedFileName.substring(slashIndex + 1);
        }
        return cleanedFileName;
    }

    private String readUtf8Text(MultipartFile file) {
        try {
            return StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(file.getBytes()))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new BadRequestException("Uploaded file must be valid UTF-8 text.");
        } catch (IOException exception) {
            throw new BadRequestException("Failed to read uploaded file.");
        }
    }
}
