package com.example.tgbot.integration.kieai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class KeiAiRequestService {
    private final HttpClient httpClient;

    @Value("${kieai.api-key}")
    private final String apiKey;
    private final String baseUrl;

    public KeiAiRequestService(@Value("${kieai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.baseUrl = "https://api.kie.ai/api/v1";
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(10000)).build();
    }

    public String sendPostRequest(String endpoint, String jsonPayload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)  // если используется Bearer token
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            return body; // успешный ответ, возвращаем тело
        } else {
            throw new RuntimeException("Failed: HTTP error code : " + statusCode + ", body: " + body);
        }
    }

    public String sendGetRequest(String endpoint, String jsonPayload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)  // если используется Bearer token
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            return body; // успешный ответ, возвращаем тело
        } else {
            throw new RuntimeException("Failed: HTTP error code : " + statusCode + ", body: " + body);
        }
    }

}
