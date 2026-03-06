package com.example.tgbot.integration.kieai;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.integration.config.IModelRequestOptions;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@Getter
public class ReceivedFile {
    @Builder.Default
    private final List<String> fileUrls = new ArrayList<>();
    private final GenerationModel model;
    private IModelRequestOptions requestOptions;

    public String getFirstUrl() {
        return fileUrls.stream().findFirst().orElse(null);
    }

    public void addResultingUrl(String url) {
        fileUrls.add(url);
    }
}
