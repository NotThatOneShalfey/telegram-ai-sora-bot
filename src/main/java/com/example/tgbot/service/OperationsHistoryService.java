package com.example.tgbot.service;

import com.example.tgbot.db.OperationsHistory;
import com.example.tgbot.db.repositories.OperationsHistoryRepository;
import com.example.tgbot.models.configurations.dto.HistoryItemDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationsHistoryService {
    private final OperationsHistoryRepository historyRepository;
    private final UserQueryService userQueryService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

    /**
     * Возвращает историю операций пользователя по userId (User.telegramId),
     * отсортированную по дате по убыванию.
     */
    public List<HistoryItemDTO> getHistory(Long userId) {
        return userQueryService.findUser(userId)
                .map(user -> {
                    List<OperationsHistory> list = historyRepository.findByUserIdOrderByOperationTimestampDesc(user);
                    return list.stream()
                            .map(this::toHistoryItemDTO)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private HistoryItemDTO toHistoryItemDTO(OperationsHistory oh) {
        Map<String, Object> options = parseOptions(oh.getGenerationRequestInput());
        List<String> resultUrls = parseResultUrls(oh.getResultUrls());
        String date = oh.getOperationTimestamp() != null
                ? oh.getOperationTimestamp().toInstant().atZone(ZoneId.systemDefault()).format(ISO_FORMATTER)
                : null;
        return new HistoryItemDTO(options, oh.getBalanceChange(), date, resultUrls);
    }

    private Map<String, Object> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse generationRequestInput as options: {}", e.getMessage());
            return null;
        }
    }

    private List<String> parseResultUrls(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> urls = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return urls != null ? urls : List.of();
        } catch (Exception e) {
            log.warn("Failed to parse resultUrls: {}", e.getMessage());
            return List.of();
        }
    }
}
