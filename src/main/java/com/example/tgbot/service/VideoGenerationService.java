package com.example.tgbot.service;

import com.example.tgbot.bot.UserSession;
import com.example.tgbot.data.GenModel;
import com.example.tgbot.data.SunoMusicGenre;
import com.example.tgbot.web.CreateTaskResponse;
import com.example.tgbot.web.RecordInfoResponse;
import com.example.tgbot.web.callbacks.keiai.KeiAiMusicCallbackResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.regex.Pattern.compile;

@Service
@Slf4j
public class VideoGenerationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, KeiAiMusicCallbackResponse> keiAiResponses = new HashMap<>();

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

    public Mono<String> generateFromPrompt(UserSession session, String prompt) {
        return switch (session.getModel()) {
            case SORA_2 -> generateVideoSora2(session, prompt);
            case NANO_BANANA -> generateImageNanoBanana(session, prompt);
            default -> null;
        };
    }

    public Mono<List<String>> generateMusicFromPrompt(UserSession session, String prompt) {
        if (session.getModel().equals(GenModel.SUNO_V5)) {
            return generateMusicSunoV5(session, prompt);
        }
        return null;
    }

    public Mono<String> generateFromPromptAndImage(UserSession session, String prompt, String imageUrl) {
        return switch (session.getModel()) {
            case KLING_3_0 -> generateVideoKling(session, prompt, imageUrl);
            case SORA_2_WITH_IMAGE -> generateVideoSora2(session, prompt, imageUrl);
            case NANO_BANANA_EDIT -> generateImageNanoBananaEdit(session, prompt, imageUrl);
            default -> null;
        };
    }

    public Mono<List<String>> generateMusicSunoV5(UserSession session, String prompt) {
        String resultingPrompt = "";
        if (session.getSelectedFormat() instanceof SunoMusicGenre g) {
            resultingPrompt = "Жанр: " + g.getLocalDesc() + ".";
        } else {
            resultingPrompt = "Жанр: " + session.getSelectedFormat() + ".";
        }
        resultingPrompt = resultingPrompt + " Описание: " + prompt;
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "V5");
        payload.put("customMode", false);
        payload.put("prompt", resultingPrompt);
        payload.put("instrumental", false);
        payload.put("audioWeight", null);
        payload.put("callBackUrl", "https://24sora2.ru/dev-webhook/keiai/callback/music");

        session.setPayload(payload);
        log.trace("Call generateMusicSunoV5. Payload={}", payload);
        return getTaskResponseForMusic(payload);
    }

    public Mono<String> generateVideoSora2(UserSession session, String prompt) {
        String format = session.getSelectedFormat().toString();
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        input.put("aspect_ratio", getAspectRatio(format));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "sora-2-text-to-video");
        payload.put("input", input);

        session.setPayload(payload);
        log.trace("Call generateVideoSora2. Payload={}", payload);
        return getTaskResponse(payload);
    }

    public Mono<String> generateVideoSora2(UserSession session, String prompt, String imageUrl) {
        String format = session.getSelectedFormat().toString();
        Map<String, Object> input = new HashMap<>();
        input.put("prompt", prompt);
        input.put("aspect_ratio", getAspectRatio(format));
        input.put("image_urls", new String[]{imageUrl});

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "sora-2-image-to-video");
        payload.put("input", input);

        session.setPayload(payload);
        log.trace("Call generateVideoSora2. Payload={}", payload);
        return getTaskResponse(payload);
    }

    public Mono<String> generateVideoKling(UserSession session, String prompt, String imageUrl) {
        String format = session.getSelectedFormat().toString();
        Map<String, Object> input = new HashMap<>();

        if (prompt != null && !prompt.isBlank()) {
            input.put("prompt", prompt);
        }
        input.put("mode", "std");
        input.put("duration", "8");
        input.put("image_urls", new String[]{imageUrl});
        input.put("aspect_ratio", format);
        input.put("multi_shots", false);
        input.put("sound", true);


        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "kling-3.0/video");
        payload.put("input", input);

        session.setPayload(payload);
        log.trace("Call generateVideoKling. Payload={}", payload);
        return getTaskResponse(payload);
    }

    public Mono<String> generateImageNanoBanana(UserSession session, String prompt) {
        String imageSize = session.getSelectedFormat().toString();
        Map<String, Object> input = new HashMap<>();

        if (prompt != null && !prompt.isBlank()) {
            input.put("prompt", prompt);
        }
        input.put("output_format", "png");
        input.put("image_size", imageSize);
        input.put("resolution", "2K");

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "nano-banana-pro");
        payload.put("input", input);

        session.setPayload(payload);
        log.trace("Call generateImageNanoBanana. Payload={}", payload);
        return getTaskResponse(payload);
    }

    public Mono<String> generateImageNanoBananaEdit(UserSession session, String prompt, String imageUrl) {
        String imageSize = session.getSelectedFormat().toString();
        Map<String, Object> input = new HashMap<>();

        if (prompt != null && !prompt.isBlank()) {
            input.put("prompt", prompt);
        }
        input.put("image_urls", new String[]{imageUrl});
        input.put("output_format", "png");
        input.put("resolution", "2K");
        input.put("image_size", imageSize);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "nano-banana-pro");
        payload.put("input", input);

        session.setPayload(payload);
        log.trace("Call generateImageNanoBananaEdit. Payload={}", payload);
        return getTaskResponse(payload);
    }




    private Mono<String> getTaskResponse(Map<String, Object> payload) {
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
                    return pollForCompletionV2(taskId);
                });
    }

    private Mono<List<String>> getTaskResponseForMusic(Map<String, Object> payload) {
        return webClient.post()
                .uri("/generate")
                .bodyValue(payload)
                .retrieve()
                .onStatus(s -> !s.is2xxSuccessful(), resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .map(body -> new IllegalStateException("Kie.ai createTask HTTP " + resp.statusCode() + " body: " + body))
                )
                .bodyToMono(CreateTaskResponse.class)
                .doOnNext(r -> log.debug("generate resp: {}", r))
                .flatMap(r -> {
                    String taskId = r.getData() != null ? r.getData().getTaskId() : null;
                    if (taskId == null || taskId.isBlank()) {
                        return Mono.error(new IllegalStateException("Kie.ai did not return taskId; resp=" + r));
                    }
                    return getMusicTaskCompletionFromCallbacks(taskId, 1);
                });
    }

    /**
     * Опрос Kie.ai о статусе задачи. Первый запрос — спустя 2 минуты,
     * затем каждые 30 секунд до получения результата или ошибки.
     */

    private Mono<String> pollForCompletionV2(String taskId) {
        AtomicInteger pollExpandCounter = new AtomicInteger(1);
        // Первичный запрос через 15 секунд
        return Mono.delay(Duration.ofSeconds(15))
                .then(fetchTaskStatus(taskId))
                .flatMap(r -> {
                    RecordInfoResponse.DataBlock d = r.getData();
                    String state = (d != null && d.getState() != null) ? d.getState().toLowerCase() : "";
                    log.trace("-> Poll #1 for response, taskId={}, response={}", taskId, r);
                    switch (state) {
                        case "success":
                            return Mono.just(r); // задача завершена
                        case "fail":
                            return Mono.error(new IllegalStateException(d.getFailMsg()));
                        case "waiting":
                        case "queuing":
                        case "generating":
                        default:
                            // Повторный запрос через 2 минуты
                            return Mono.delay(Duration.ofMinutes(2))
                                    .then(fetchTaskStatus(taskId));
                    }
                })
                .expand(r -> {
                    RecordInfoResponse.DataBlock d = r.getData();
                    String state = (d != null && d.getState() != null) ? d.getState().toLowerCase() : "";
                    log.trace("-> Poll #{} for response, taskId={}, response={}", pollExpandCounter.incrementAndGet(), taskId, r);
                    switch (state) {
                        case "success":
                            return Mono.empty(); // задача завершена
                        case "fail":
                            return Mono.error(new IllegalStateException(d.getFailMsg()));
                        case "waiting":
                        case "queuing":
                        case "generating":
                        default:
                            // повторный опрос через 30 секунд
                            return Mono.delay(Duration.ofSeconds(30))
                                    .then(fetchTaskStatus(taskId));
                    }
                })
                .last()
                .map(this::extractUrlFromRecordInfo);

    }

    private Mono<List<String>> getMusicTaskCompletionFromCallbacks(String taskId, int pollNumber) {
        log.trace("getMusicTaskCompletionFromCallbacks -> Poll #{} for response, taskId={}", pollNumber, taskId);
        List<String> urlResponses = new ArrayList<>();
        if (keiAiResponses.get(taskId) != null) {
            keiAiResponses.get(taskId).getData().getData().forEach(d -> urlResponses.add(d.getAudioUrl()));
            return Mono.just(urlResponses);
        }
        return Mono.delay(Duration.ofSeconds(15)).then(getMusicTaskCompletionFromCallbacks(taskId, pollNumber+1));
    }

    public void putCallbackResponse(KeiAiMusicCallbackResponse response) {
        if (response.getData().getCallbackType().equals("complete")) {
            keiAiResponses.put(response.getData().getTaskId(), response);
        }
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

    private String getAspectRatio(String format) {
        // Correctly map aspect ratios: 16:9 -> landscape; 9:16 -> portrait【129760953625935†L135-L140】
        String aspectRatio = "portrait";
        if (format != null) {
            aspectRatio = switch (format) {
                case "16:9" -> "landscape";
                case "9:16" -> "portrait";
                default -> "portrait";
            };
        }
        return aspectRatio;
    }
}