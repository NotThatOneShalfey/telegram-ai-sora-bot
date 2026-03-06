package com.example.tgbot.service;

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

    public User findUserByUserName(String userName) {
        return userQueryService.findUserByUserName(userName);
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
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        user.setBalance(user.getBalance() + amount);

        historyRepository.save(OperationsHistory.builder()
                .balanceChange((float) amount)
                .userId(user)
                .generationRequestInput(null)
                .operationType(HistoryOperationType.BALANCE_CHANGE)
                .build());
        return userRepository.save(user);
    }

    @Transactional
    public User putOnHold(UserSession session, int price, Map<String, Object> requestPayload) {
        User user = session.getUser();
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
        User user = session.getUser();
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
                                     List<String> resultUrls, GenerationModel model, BigDecimal costRub) {
        User user = session.getUser();
        if (user.getBalanceHold() >= price) {
            user.setBalanceHold(user.getBalanceHold() - price);
        }
        try {
            String resultUrlsJson = resultUrls != null && !resultUrls.isEmpty()
                    ? objectMapper.writeValueAsString(resultUrls)
                    : null;
            historyRepository.save(OperationsHistory.builder()
                    .balanceChange((float) (price * -1))
                    .userId(user)
                    .generationRequestInput(objectMapper.writeValueAsString(requestPayload))
                    .resultUrls(resultUrlsJson)
                    .operationType(HistoryOperationType.GENERATION_REQUEST)
                    .generationType(model != null ? model.getGenerationType() : null)
                    .model(model)
                    .costRub(costRub != null ? costRub : BigDecimal.ZERO)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User addGift(User user) {
        user.setBalance(user.getBalance() + 100);
        user.setBonusReceived(true);
        historyRepository.save(OperationsHistory.builder()
                .balanceChange(100F)
                .userId(user)
                .generationRequestInput(null)
                .operationType(HistoryOperationType.GIFT)
                .build());
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
        User user = session.getUser();
        int current = user.getBalance();
        return current >= price;
    }
}