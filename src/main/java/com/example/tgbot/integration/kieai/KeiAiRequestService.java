package com.example.tgbot.integration.kieai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
public class KeiAiRequestService {
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 2000;

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final int maxRetries;
    private final long retryDelayMs;

    public KeiAiRequestService(@Value("${kieai.api-key}") String apiKey,
                               @Value("${kieai.http.max-retries:" + DEFAULT_MAX_RETRIES + "}") int maxRetries,
                               @Value("${kieai.http.retry-delay-ms:" + DEFAULT_RETRY_DELAY_MS + "}") long retryDelayMs) {
        this.apiKey = apiKey;
        this.baseUrl = "https://api.kie.ai/api/v1";
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(10000)).build();
    }

    public String sendPostRequest(String endpoint, String jsonPayload) throws Exception {
        String url = baseUrl + endpoint;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        // Логирование CURL
        String curl = toCurl(url, jsonPayload);
        log.debug("Kei AI request (curl): {}", curl);

        int lastAttempt = maxRetries + 1;

        for (int attempt = 1; attempt <= lastAttempt; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                String body = response.body();

                if (statusCode >= 200 && statusCode < 300) {
                    return body;
                }
                throw new RuntimeException("Failed: HTTP error code : " + statusCode + ", body: " + body);
            } catch (IOException e) {
                if (attempt < lastAttempt) {
                    log.warn("Kei AI HTTP request failed (attempt {}/{}): {} — retrying in {} ms", attempt, lastAttempt, e.getMessage(), retryDelayMs);
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Request interrupted during retry delay", ie);
                    }
                } else {
                    log.error("Kei AI HTTP request failed after {} attempts", lastAttempt, e);
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request interrupted", e);
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    /** Собирает строку curl для запроса (API key в логе маскируется). */
    private String toCurl(String url, String jsonPayload) {
        String escaped = jsonPayload == null ? "" : jsonPayload.replace("'", "'\\''");
        String authMask = apiKey == null || apiKey.isEmpty() ? "" : "Bearer ***";
        return "curl -X POST '" + url + "' "
                + "-H 'Content-Type: application/json' "
                + "-H 'Authorization: " + authMask + "' "
                + "-d '" + escaped + "'";
    }
}
