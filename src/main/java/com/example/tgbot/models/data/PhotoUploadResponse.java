package com.example.tgbot.models.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhotoUploadResponse {
    private String url;
    private String filename;
}
