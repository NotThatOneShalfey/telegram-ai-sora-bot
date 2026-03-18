package com.example.tgbot.integration.seedance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * HTTP-клиент для Seedance 2.0 API (seedance2-pro.com).
 */
@Service
@Slf4j
public class SeedanceRequestService {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;

    public SeedanceRequestService(@Value("${seedance.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.baseUrl = "https://seedance2-pro.com/api/v1";
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(10000)).build();
    }

    /**
     * Создаёт задачу генерации. POST /jobs/createTask
     *
     * @return JSON ответ, например {"taskId":"task_xxx"}
     */
    public String createTask(String jsonPayload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/jobs/createTask"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            return body;
        }
        throw new RuntimeException("Seedance createTask failed: HTTP " + statusCode + ", body: " + body);
    }

    /**
     * Получает статус задачи. GET /jobs/recordInfo?taskId=...
     *
     * @return JSON ответ с полями status, output, error и т.д.
     */
    public String recordInfo(String taskId) throws IOException, InterruptedException {
        String encodedTaskId = URLEncoder.encode(taskId, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/jobs/recordInfo?taskId=" + encodedTaskId))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            return body;
        }
        throw new RuntimeException("Seedance recordInfo failed: HTTP " + statusCode + ", body: " + body);
    }
}
