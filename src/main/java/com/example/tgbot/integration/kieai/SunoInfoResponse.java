package com.example.tgbot.integration.kieai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class SunoInfoResponse {
    private int code;
    @JsonProperty("msg")
    private String message;
    private SunoData data;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SunoData {
        private String callbackType;
        private Collection<DataBlock> data;
        @JsonProperty("task_id")
        private String taskId;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataBlock {
        @JsonProperty("audio_url")
        private String audioUrl;
        private double duration;
        private String id;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("model_name")
        private String modelName;
        private String prompt;
        @JsonProperty("source_audio_url")
        private String sourceAudioUrl;
        @JsonProperty("source_image_url")
        private String sourceImageUrl;
        @JsonProperty("source_stream_audio_url")
        private String sourceStreamAudioUrl;
        @JsonProperty("stream_audio_url")
        private String streamAudioUrl;
        private String tags;
        private String title;
    }
}
