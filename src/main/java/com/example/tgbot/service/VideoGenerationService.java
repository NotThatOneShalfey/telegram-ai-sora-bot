package com.example.tgbot.service;

import com.example.tgbot.web.CreateTaskResponse;
import com.example.tgbot.web.RecordInfoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VideoGenerationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoGenerationService(@Value("${kieai.api-key}") String apiKey) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        this.webClient = WebClient.builder()
                .baseUrl("https://api.kie.ai/api/v1")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<String> generateVideoFromText(String format, String prompt) {


        String aspectRatio = "portrait";
        if (format != null) {
            switch (format) {
                case "16:9":
                    aspectRatio = "landscape";
                    break;
                case "9:16":
                    aspectRatio = "portrait";
                    break;
                default:
                    aspectRatio = "portrait";
            }
        }

        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        input.put("aspect_ratio", aspectRatio);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "sora-2-text-to-video");
        payload.put("input", input);

        return webClient.post()
                .uri("/jobs/createTask")
                .bodyValue(payload)
                .retrieve()
                .onStatus(s -> !s.is2xxSuccessful(), resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .map(body -> new IllegalStateException("Kie.ai createTask HTTP " + resp.statusCode() + " body: " + body))
                )
                .bodyToMono(CreateTaskResponse.class)
                .doOnNext(r -> log.debug("createTask resp: {}", r))
                .flatMap(r -> {
                    String taskId = r.getData() != null ? r.getData().getTaskId() : null;
                    if (taskId == null || taskId.isBlank()) {
                        return Mono.error(new IllegalStateException("Kie.ai did not return taskId; resp=" + r));
                    }
                    return pollForCompletion(taskId);
                });
    }

    public Mono<String> generateVideoFromImage(String format, String prompt, String imageUrl) {
        // Correctly map aspect ratios: 16:9 -> landscape; 9:16 -> portrait【129760953625935†L135-L140】
        String aspectRatio = "portrait";
        if (format != null) {
            switch (format) {
                case "16:9":
                    aspectRatio = "landscape";
                    break;
                case "9:16":
                    aspectRatio = "portrait";
                    break;
                default:
                    aspectRatio = "portrait";
            }
        }

        Map<String, Object> input = new HashMap<>();

        if (prompt != null && !prompt.isBlank()) {
            input.put("prompt", prompt);
        }
        input.put("image_urls", new String[]{imageUrl});
        input.put("aspect_ratio", aspectRatio);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "sora-2-image-to-video");
        payload.put("input", input);
        log.debug(payload.toString());
        return webClient.post()
                .uri("/jobs/createTask")
                .bodyValue(payload)
                .retrieve()
                .onStatus(s -> !s.is2xxSuccessful(), resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .map(body -> new IllegalStateException("Kie.ai createTask HTTP " + resp.statusCode() + " body: " + body))
                )
                .bodyToMono(CreateTaskResponse.class)
                .doOnNext(r -> log.debug("createTask resp: {}", r))
                .flatMap(r -> {
                    String taskId = r.getData() != null ? r.getData().getTaskId() : null;
                    if (taskId == null || taskId.isBlank()) {
                        return Mono.error(new IllegalStateException("Kie.ai did not return taskId; resp=" + r));
                    }
                    return pollForCompletion(taskId);
                });
    }

    /**
     * Опрос Kie.ai о статусе задачи. Первый запрос — спустя 2 минуты,
     * затем каждые 30 секунд до получения результата или ошибки.
     */
    private Mono<String> pollForCompletion(String taskId) {
        return Mono.delay(Duration.ofMinutes(2))
                .then(fetchTaskStatus(taskId))
                .expand(resp -> {
                    RecordInfoResponse.DataBlock d = resp.getData();
                    String state = (d != null && d.getState() != null) ? d.getState().toLowerCase() : "";
                    switch (state) {
                        case "success":
                            return Mono.empty(); // задача завершена
                        case "failed":
                            return Mono.error(new IllegalStateException("Kie.ai task failed with state=" + state));
                        case "waiting":
                        case "queuing":
                        case "generating":
                        default:
                            // повторный опрос через 30 секунд
                            return Mono.delay(Duration.ofSeconds(30)).then(fetchTaskStatus(taskId));
                    }
                })
                .last()
                .map(this::extractUrlFromRecordInfo);
    }


    private Mono<RecordInfoResponse> fetchTaskStatus(String taskId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/jobs/recordInfo").queryParam("taskId", taskId).build())
                .retrieve()
                .bodyToMono(RecordInfoResponse.class)
                .onErrorResume(e -> Mono.error(new IllegalStateException("Error contacting Kie.ai: " + e.getMessage(), e)));
    }

    private String extractUrlFromRecordInfo(RecordInfoResponse resp) {
        RecordInfoResponse.DataBlock d = resp.getData();
        if (d == null || d.getResultJson() == null || d.getResultJson().isBlank()) {
            throw new IllegalStateException("recordInfo has no data/resultJson: " + resp);
        }

        String resultJsonStr = d.getResultJson();
        try {
            JsonNode root = objectMapper.readTree(resultJsonStr);

            JsonNode urls = root.path("resultUrls");
            if (urls.isArray() && urls.size() > 0) {
                String url = urls.get(0).asText(null);
                if (url != null && !url.isBlank()) return url;
            }


            JsonNode wm = root.path("resultWaterMarkUrls");
            if (wm.isArray() && wm.size() > 0) {
                String url = wm.get(0).asText(null);
                if (url != null && !url.isBlank()) return url;
            }

            throw new IllegalStateException("No result url in resultJson: " + resultJsonStr);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse resultJson: " + resultJsonStr, e);
        }
    }
}