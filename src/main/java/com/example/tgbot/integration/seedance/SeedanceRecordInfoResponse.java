package com.example.tgbot.integration.seedance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Ответ GET /jobs/recordInfo Seedance API.
 * status: waiting | generating | success | fail
 * output: [{url, width, height}] при success
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class SeedanceRecordInfoResponse {

    private String taskId;
    private String model;
    private String status;
    private Integer creditsUsed;
    private List<OutputItem> output;
    private String error;
    private Long createTime;
    private Long completeTime;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutputItem {
        private String url;
        private Integer width;
        private Integer height;
    }
}
