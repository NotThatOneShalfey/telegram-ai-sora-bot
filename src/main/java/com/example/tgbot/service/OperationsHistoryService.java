package com.example.tgbot.service;

import com.example.tgbot.domain.enums.GenerationType;
import com.example.tgbot.domain.model.OperationsHistory;
import com.example.tgbot.dto.api.HistoryItemDTO;
import com.example.tgbot.dto.api.HistoryResponseDTO;
import com.example.tgbot.repository.OperationsHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
     * Возвращает историю операций пользователя по userId (User.telegramId) и типу генерации,
     * с текущим балансом и списком записей, отсортированным по дате по убыванию.
     */
    public HistoryResponseDTO getHistory(Long userId, GenerationType generationType) {
        return userQueryService.findUser(userId)
                .map(user -> {
                    List<OperationsHistory> list = generationType == null
                            ? historyRepository.findByUserIdOrderByOperationTimestampDesc(user)
                            : historyRepository.findByUserIdAndGenerationTypeOrderByOperationTimestampDesc(user, generationType);
                    List<HistoryItemDTO> items = list.stream()
                            .map(this::toHistoryItemDTO)
                            .toList();
                    Integer balance = user.getBalance() != null ? user.getBalance() : 0;
                    return new HistoryResponseDTO(balance, items);
                })
                .orElse(new HistoryResponseDTO(null, Collections.emptyList()));
    }

    private HistoryItemDTO toHistoryItemDTO(OperationsHistory oh) {
        Map<String, Object> options = parseOptions(oh.getGenerationRequestInput());
        List<?> resultItems = parseResultItems(oh.getResultUrls());
        String date = oh.getOperationTimestamp() != null
                ? oh.getOperationTimestamp().toInstant().atZone(ZoneId.systemDefault()).format(ISO_FORMATTER)
                : null;
        String model = oh.getModel() != null ? oh.getModel().name() : null;
        return new HistoryItemDTO(options, oh.getBalanceChange(), date, resultItems, model);
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

    private List<?> parseResultItems(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isArray()) {
                return List.of();
            }
            return objectMapper.convertValue(node, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse resultItems: {}", e.getMessage());
            return List.of();
        }
    }
}
