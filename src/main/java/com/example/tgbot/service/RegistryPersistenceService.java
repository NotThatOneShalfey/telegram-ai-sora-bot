package com.example.tgbot.service;

import com.example.tgbot.domain.model.AppRegistrySeedancePolling;
import com.example.tgbot.domain.model.AppRegistryTaskError;
import com.example.tgbot.domain.model.AppRegistryTaskResult;
import com.example.tgbot.domain.model.AppRegistryWaitingSession;
import com.example.tgbot.domain.model.User;
import com.example.tgbot.domain.value.TaskSource;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.registry.TaskErrorRegistry;
import com.example.tgbot.registry.TaskResultRegistry;
import com.example.tgbot.repository.AppRegistrySeedancePollingRepository;
import com.example.tgbot.repository.AppRegistryTaskErrorRepository;
import com.example.tgbot.repository.AppRegistryTaskResultRepository;
import com.example.tgbot.repository.AppRegistryWaitingSessionRepository;
import com.example.tgbot.repository.UserRepository;
import com.example.tgbot.telegram.session.UserSession;
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
import java.util.stream.Collectors;

/**
 * Сохраняет состояние регистров (Seedance polling, task result, task error, waiting sessions) в БД при shutdown
 * и восстанавливает при старте. После восстановления таблицы очищаются.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RegistryPersistenceService {

    private final SeedancePollingRegistry seedancePollingRegistry;
    private final TaskResultRegistry taskResultRegistry;
    private final TaskErrorRegistry taskErrorRegistry;
    private final SessionRegistry sessionRegistry;
    private final AppRegistrySeedancePollingRepository seedancePollingRepo;
    private final AppRegistryTaskResultRepository taskResultRepo;
    private final AppRegistryTaskErrorRepository taskErrorRepo;
    private final AppRegistryWaitingSessionRepository waitingSessionRepo;
    private final UserRepository userRepository;
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

            List<AppRegistryWaitingSession> waitingList = waitingSessionRepo.findAll();
            Map<Long, List<AppRegistryWaitingSession>> byUserId = waitingList.stream()
                    .collect(Collectors.groupingBy(AppRegistryWaitingSession::getUserId));
            for (Map.Entry<Long, List<AppRegistryWaitingSession>> entry : byUserId.entrySet()) {
                Long userId = entry.getKey();
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    log.warn("Skip restore waiting sessions: user not found for id={}", userId);
                    continue;
                }
                UserSession session = new UserSession(user);
                for (AppRegistryWaitingSession row : entry.getValue()) {
                    Map<String, Object> requestInput = parseOptionsJson(row.getOptionsJson());
                    session.putRestoredPendingTask(row.getTaskId(), row.getModel(), requestInput);
                    TaskSource source = TaskSource.valueOf(row.getSource());
                    sessionRegistry.putWaitingSession(row.getTaskId(), session, source);
                }
            }
            if (!waitingList.isEmpty()) {
                log.info("Restored {} waiting sessions from DB", waitingList.size());
            }

            seedancePollingRepo.deleteAll();
            taskResultRepo.deleteAll();
            taskErrorRepo.deleteAll();
            waitingSessionRepo.deleteAll();
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

            List<AppRegistryWaitingSession> waitingEntities = new ArrayList<>();
            for (SessionRegistry.WaitingSessionSnapshot snap : sessionRegistry.getAllWaitingSnapshot()) {
                UserSession session = snap.session();
                if (session == null || session.getUser() == null) {
                    continue;
                }
                IModelRequestOptions options = session.getRequestOptionsByTaskIdAndModel(snap.taskId());
                if (options == null) {
                    continue;
                }
                String optionsJson = objectMapper.writeValueAsString(options.getRequestInput());
                waitingEntities.add(AppRegistryWaitingSession.builder()
                        .taskId(snap.taskId())
                        .userId(session.getUser().getId())
                        .source(snap.source().name())
                        .model(options.getModel())
                        .optionsJson(optionsJson)
                        .build());
            }
            waitingSessionRepo.saveAll(waitingEntities);
            if (!waitingEntities.isEmpty()) {
                log.info("Saved {} waiting sessions to DB", waitingEntities.size());
            }
        } catch (Exception e) {
            log.warn("Failed to save registries to DB: {}", e.getMessage());
        }
    }

    private Map<String, Object> parseOptionsJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse optionsJson: {}", e.getMessage());
            return Map.of();
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
