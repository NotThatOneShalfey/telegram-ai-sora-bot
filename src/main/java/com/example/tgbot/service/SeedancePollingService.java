package com.example.tgbot.service;

import com.example.tgbot.integration.seedance.SeedanceRecordInfoResponse;
import com.example.tgbot.integration.seedance.SeedanceRequestService;
import com.example.tgbot.telegram.handler.CallbackHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Polling-сервис для задач Seedance 2.0.
 * Первый запрос — через 1 мин после создания, далее каждые 30 сек.
 * Без React/Mono.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeedancePollingService {

    private final SeedancePollingRegistry pollingRegistry;
    private final SeedanceRequestService requestService;
    private final CallbackHandler callbackHandler;

    private final ObjectMapper mapper = new JsonMapper();

    @Scheduled(fixedDelay = 15_000)
    public void pollTasks() {
        Set<String> due = pollingRegistry.getTasksDueForPoll();
        if (due.isEmpty()) {
            return;
        }
        for (String taskId : due) {
            try {
                String body = requestService.recordInfo(taskId);
                SeedanceRecordInfoResponse resp = mapper.readValue(body, SeedanceRecordInfoResponse.class);
                pollingRegistry.markPolled(taskId);
                String status = resp.getStatus();
                if ("success".equals(status)) {
                    var output = resp.getOutput();
                    if (output != null && !output.isEmpty() && output.get(0).getUrl() != null) {
                        callbackHandler.handleSeedancePollResult(taskId, status, output, resp.getError());
                        pollingRegistry.unregister(taskId);
                    }
                } else if ("fail".equals(status)) {
                    callbackHandler.handleSeedancePollResult(taskId, status, null, resp.getError());
                    pollingRegistry.unregister(taskId);
                }
            } catch (Exception e) {
                log.warn("Seedance poll failed for taskId={}: {}", taskId, e.getMessage());
            }
        }
    }
}
