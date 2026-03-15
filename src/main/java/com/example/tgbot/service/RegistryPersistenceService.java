package com.example.tgbot.service;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.model.AppRegistrySeedancePolling;
import com.example.tgbot.domain.model.AppRegistryTaskError;
import com.example.tgbot.domain.model.AppRegistryTaskResult;
import com.example.tgbot.domain.value.ErrorCode;
import com.example.tgbot.registry.TaskErrorRegistry;
import com.example.tgbot.registry.TaskResultRegistry;
import com.example.tgbot.repository.AppRegistrySeedancePollingRepository;
import com.example.tgbot.repository.AppRegistryTaskErrorRepository;
import com.example.tgbot.repository.AppRegistryTaskResultRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Сохраняет состояние регистров (Seedance polling, task result, task error) в БД при shutdown
 * и восстанавливает при старте. SessionRegistry не сохраняется.
 * После восстановления таблицы очищаются.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RegistryPersistenceService {

    private final SeedancePollingRegistry seedancePollingRegistry;
    private final TaskResultRegistry taskResultRegistry;
    private final TaskErrorRegistry taskErrorRegistry;
    private final AppRegistrySeedancePollingRepository seedancePollingRepo;
    private final AppRegistryTaskResultRepository taskResultRepo;
    private final AppRegistryTaskErrorRepository taskErrorRepo;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void loadFromDatabase() {
        try {
            List<AppRegistrySeedancePolling> seedanceList = seedancePollingRepo.findAll();
            for (AppRegistrySeedancePolling e : seedanceList) {
                seedancePollingRegistry.registerWithTimes(e.getTaskId(), e.getCreatedAt(), e.getLastPolledAt());
            }
            if (!seedanceList.isEmpty()) {
                log.info("Restored {} Seedance polling tasks from DB", seedanceList.size());
            }

            List<AppRegistryTaskResult> resultList = taskResultRepo.findAll();
            for (AppRegistryTaskResult e : resultList) {
                List<Object> resultItems = parseResultItemsJson(e.getResultItemsJson());
                taskResultRegistry.put(e.getTaskId(), new TaskResultRegistry.TaskResultRecord(
                        e.getUserId(), e.getModel(), e.getOptionsJson(), resultItems, e.getBalanceChange()));
            }
            if (!resultList.isEmpty()) {
                log.info("Restored {} task results from DB", resultList.size());
            }

            List<AppRegistryTaskError> errorList = taskErrorRepo.findAll();
            for (AppRegistryTaskError e : errorList) {
                taskErrorRegistry.put(e.getTaskId(), e.getUserId(), e.getErrorCode());
            }
            if (!errorList.isEmpty()) {
                log.info("Restored {} task errors from DB", errorList.size());
            }

            seedancePollingRepo.deleteAll();
            taskResultRepo.deleteAll();
            taskErrorRepo.deleteAll();
            log.debug("Cleared registry tables after restore");
        } catch (Exception e) {
            log.warn("Failed to restore registries from DB: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void saveToDatabase() {
        try {
            List<AppRegistrySeedancePolling> seedanceEntities = new ArrayList<>();
            for (SeedancePollingRegistry.PollSnapshot s : seedancePollingRegistry.getSnapshotForPersistence()) {
                seedanceEntities.add(AppRegistrySeedancePolling.builder()
                        .taskId(s.taskId())
                        .createdAt(s.createdAt())
                        .lastPolledAt(s.lastPolledAt())
                        .build());
            }
            seedancePollingRepo.saveAll(seedanceEntities);
            if (!seedanceEntities.isEmpty()) {
                log.info("Saved {} Seedance polling tasks to DB", seedanceEntities.size());
            }

            List<AppRegistryTaskResult> resultEntities = new ArrayList<>();
            for (Map.Entry<String, TaskResultRegistry.TaskResultRecord> e : taskResultRegistry.getAllSnapshot().entrySet()) {
                TaskResultRegistry.TaskResultRecord r = e.getValue();
                String resultItemsJson = r.getResultItems() != null ? objectMapper.writeValueAsString(r.getResultItems()) : null;
                resultEntities.add(AppRegistryTaskResult.builder()
                        .taskId(e.getKey())
                        .userId(r.getUserId())
                        .model(r.getModel())
                        .optionsJson(r.getOptionsJson())
                        .resultItemsJson(resultItemsJson)
                        .balanceChange(r.getBalanceChange())
                        .build());
            }
            taskResultRepo.saveAll(resultEntities);
            if (!resultEntities.isEmpty()) {
                log.info("Saved {} task results to DB", resultEntities.size());
            }

            List<AppRegistryTaskError> errorEntities = new ArrayList<>();
            for (Map.Entry<String, TaskErrorRegistry.TaskErrorRecord> e : taskErrorRegistry.getAllSnapshot().entrySet()) {
                TaskErrorRegistry.TaskErrorRecord r = e.getValue();
                errorEntities.add(AppRegistryTaskError.builder()
                        .taskId(e.getKey())
                        .userId(r.getUserId())
                        .errorCode(r.getErrorCode())
                        .build());
            }
            taskErrorRepo.saveAll(errorEntities);
            if (!errorEntities.isEmpty()) {
                log.info("Saved {} task errors to DB", errorEntities.size());
            }
        } catch (Exception e) {
            log.warn("Failed to save registries to DB: {}", e.getMessage());
        }
    }

    private List<Object> parseResultItemsJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse resultItemsJson: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
