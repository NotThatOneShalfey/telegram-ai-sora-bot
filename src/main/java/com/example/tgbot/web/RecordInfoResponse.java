package com.example.tgbot.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RecordInfoResponse {
    private int code;
    private String message;
    private DataBlock data;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataBlock {
        @JsonProperty("taskId")
        private String taskId;
        private String model;
        /** Статус задачи: success/failed/… */
        private String state;
        private String param;
        /**
         * В ответах Kie.ai это СТРОКА с JSON внутри!
         * Пример: {"resultUrls":["https://...mp4"],"resultWaterMarkUrls":[...]}
         */
        @JsonProperty("resultJson")
        private String resultJson;

        @JsonProperty("failCode")
        private String failCode;

        @JsonProperty("failMsg")
        private String failMsg;
        private Long completeTime;
        private Long createTime;
        private Long updateTime;
    }
}