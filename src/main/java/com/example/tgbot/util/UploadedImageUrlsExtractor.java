package com.example.tgbot.util;

import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Извлекает URL загруженных файлов (изображения, видео) из options запроса для последующего удаления.
 */
@Component
@RequiredArgsConstructor
public class UploadedImageUrlsExtractor {
    private static final String KEY_IMAGE_URLS = "image_urls";
    private static final String KEY_IMAGE_INPUT = "image_input";
    private static final String KEY_INPUT_URLS = "input_urls";
    private static final String KEY_VIDEO_URLS = "video_urls";

    private final UploadService uploadService;

    /**
     * Возвращает список URL наших загруженных файлов из requestOptions.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractOurUploadedUrls(IModelRequestOptions requestOptions) {
        List<String> result = new ArrayList<>();
        Map<String, Object> input = requestOptions.getRequestInput();
        if (input == null) return result;

        for (String key : List.of(KEY_IMAGE_URLS, KEY_IMAGE_INPUT, KEY_INPUT_URLS, KEY_VIDEO_URLS)) {
            Object val = input.get(key);
            if (val instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof String url && uploadService.isOurUrl(url)) {
                        result.add(url);
                    }
                }
            }
        }
        return result;
    }
}
