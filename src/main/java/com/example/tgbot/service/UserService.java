package com.example.tgbot.service;

import com.example.tgbot.domain.enums.GenerationStatus;
import com.example.tgbot.domain.enums.HistoryOperationType;
import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.model.OperationsHistory;
import com.example.tgbot.domain.model.ReferralLinks;
import com.example.tgbot.domain.model.User;
import com.example.tgbot.repository.OperationsHistoryRepository;
import com.example.tgbot.repository.ReferralLinksRepository;
import com.example.tgbot.repository.UserRepository;
import com.example.tgbot.telegram.session.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserQueryService userQueryService;
    private final UserRepository userRepository;
    private final OperationsHistoryRepository historyRepository;
    private final ReferralLinksRepository referralLinksRepository;
    private final ObjectMapper objectMapper;

    /** Делегирует запрос к UserQueryService, при отсутствии — логирует и возвращает null. */
    public User findUser(Long telegramId) {
        return userQueryService.findUser(telegramId)
                .orElseGet(() -> {
                    log.error("User tgId = {} NOT FOUND!!!", telegramId);
                    return null;
                });
    }

    public User findOrCreateUser(Long chatId) {
        return userQueryService.findOrCreateUser(chatId);
    }

    @Transactional
    public void updateUserCredentials(User user, String userName, String referralLink) {
        Optional<User> existing = userQueryService.findById(user.getId());
        if (existing.isPresent()) {
            User userForUpdate = existing.get();
            boolean changed = false;
            if (!Objects.equals(userForUpdate.getUserName(), userName)) {
                userForUpdate.setUserName(userName);
                changed = true;
            }
            if (!Objects.equals(userForUpdate.getLinkUsed(), referralLink)) {
                if (checkReferral(referralLink)) {
                    userForUpdate.setLinkUsed(referralLink);
                    changed = true;
                }
            }
            // Если что нибудь поменялось, то сохраняем, если нет, то ничего не делаем
            if (changed) {
                userRepository.save(userForUpdate);
            }
        }
    }

    @Transactional
    public User addBalance(User user, int amount) {
        User existing = userQueryService.findById(user.getId()).orElse(null);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        existing.setBalance(existing.getBalance() + amount);

        historyRepository.save(OperationsHistory.builder()
                .balanceChange((float) amount)
                .userId(existing)
                .generationRequestInput(null)
                .operationType(HistoryOperationType.BALANCE_CHANGE)
                .build());
        return userRepository.save(existing);
    }

    @Transactional
    public User putOnHold(UserSession session, int price, Map<String, Object> requestPayload) {
        User user = userQueryService.findById(session.getUser().getId()).orElse(null);
        if (price <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        user.setBalance(user.getBalance() - price);
        user.setBalanceHold(user.getBalanceHold() + price);

        try {
            historyRepository.save(OperationsHistory.builder()
                    .balanceChange((float) (price * -1))
                    .userId(user)
                    .generationRequestInput(objectMapper.writeValueAsString(requestPayload))
                    .operationType(HistoryOperationType.PAYMENT_ON_HOLD)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userRepository.save(user);
    }
    @Transactional
    public User rechargeFromHold(UserSession session, int price, Map<String, Object> requestPayload) {
        User user = userQueryService.findById(session.getUser().getId()).orElse(null);
        user.setBalance(user.getBalance() + price);
        user.setBalanceHold(user.getBalanceHold() - price);

        try {
            historyRepository.save(OperationsHistory.builder()
                    .balanceChange((float) (price * -1))
                    .userId(user)
                    .generationRequestInput(objectMapper.writeValueAsString(requestPayload))
                    .operationType(HistoryOperationType.RESTORE_HOLD_PAYMENT)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User consumeOneGeneration(UserSession session, int price, Map<String, Object> requestPayload,
                                     List<?> resultItems, GenerationModel model, BigDecimal costRub, String taskId) {
        User user = userQueryService.findById(session.getUser().getId()).orElse(null);
        if (user.getBalanceHold() != null && user.getBalanceHold() >= price) {
            user.setBalanceHold(user.getBalanceHold() - price);
        }
        try {
            String resultUrlsJson = resultItems != null && !resultItems.isEmpty()
                    ? objectMapper.writeValueAsString(resultItems)
                    : null;
            updateGenerationHistoryToSuccess(taskId, resultUrlsJson, (float) (price * -1), costRub);
        } catch (Exception e) {
            log.error("Failed to update generation history to success", e);
            throw new RuntimeException(e);
        }
        return userRepository.save(user);
    }

    public boolean checkReferral(String referralLink) {
        ReferralLinks link = referralLinksRepository.findByLink(referralLink).orElse(null);
        return link != null;
    }

    @Transactional
    public ReferralLinks addReferral(String creatorUserName, String link) {
        User user = userQueryService.findUserByUserName(creatorUserName);
        ReferralLinks newLink = new ReferralLinks();
        newLink.setLink(link);
        newLink.setCreated_by(user);
        return referralLinksRepository.save(newLink);
    }

    public boolean checkBalanceBeforeGeneration(UserSession session, int price) {
        User user = userQueryService.findById(session.getUser().getId()).orElse(null);
        int current = user.getBalance();
        return current >= price;
    }

    /** Создаёт запись истории генерации со статусом REQUESTED (перед вызовом API). */
    @Transactional
    public OperationsHistory createGenerationHistoryRequested(User user, GenerationModel model, Map<String, Object> requestPayload) {
        try {
            OperationsHistory oh = OperationsHistory.builder()
                    .userId(user)
                    .operationType(HistoryOperationType.GENERATION_REQUEST)
                    .generationType(model != null ? model.getGenerationType() : null)
                    .model(model)
                    .generationRequestInput(objectMapper.writeValueAsString(requestPayload))
                    .status(GenerationStatus.REQUESTED)
                    .costRub(BigDecimal.ZERO)
                    .build();
            return historyRepository.save(oh);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /** Обновляет запись до PROCESSING и сохраняет task_id. */
    @Transactional
    public void updateGenerationHistoryToProcessing(UUID historyId, String taskId) {
        historyRepository.findById(historyId).ifPresent(oh -> {
            oh.setTaskId(taskId);
            oh.setStatus(GenerationStatus.PROCESSING);
            historyRepository.save(oh);
        });
    }

    /** Обновляет запись до SUCCESS (по taskId). */
    @Transactional
    public void updateGenerationHistoryToSuccess(String taskId, String resultUrls, float balanceChange, BigDecimal costRub) {
        historyRepository.findByTaskId(taskId).ifPresent(oh -> {
            oh.setResultUrls(resultUrls);
            oh.setBalanceChange(balanceChange);
            oh.setCostRub(costRub != null ? costRub : BigDecimal.ZERO);
            oh.setStatus(GenerationStatus.SUCCESS);
            historyRepository.save(oh);
        });
    }

    /** Устанавливает статус FAILED по historyId (ошибка на этапе запроса). */
    @Transactional
    public void updateGenerationHistoryToFailed(UUID historyId) {
        historyRepository.findById(historyId).ifPresent(oh -> {
            oh.setStatus(GenerationStatus.FAILED);
            historyRepository.save(oh);
        });
    }

    /** Устанавливает статус FAILED по taskId (ошибка на этапе получения результата). */
    @Transactional
    public void updateGenerationHistoryToFailed(String taskId) {
        historyRepository.findByTaskId(taskId).ifPresent(oh -> {
            oh.setStatus(GenerationStatus.FAILED);
            historyRepository.save(oh);
        });
    }
}