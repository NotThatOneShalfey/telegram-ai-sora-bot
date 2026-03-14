package com.example.tgbot.service;

import com.example.tgbot.registry.SessionRegistry;
import com.example.tgbot.telegram.TgBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Сервис периодической очистки: устаревших UserSession и загруженных файлов.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private static final int SESSION_STALE_DAYS = 14;
    private static final int FILE_STALE_DAYS = 7;

    private final TgBot tgBot;
    private final SessionRegistry sessionRegistry;
    private final UploadService uploadService;

    /** Удаление неактивных UserSession (старше 14 дней). Запуск раз в сутки. */
    @Scheduled(cron = "0 0 3 * * *") // 03:00 каждый день
    public void cleanStaleUserSessions() {
        log.trace("Running stale user sessions cleanup");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(SESSION_STALE_DAYS);
        int fromMain = tgBot.cleanSessionsOlderThan(cutoff);
        int fromWaiting = sessionRegistry.removeWaitingSessionsOlderThan(cutoff);
        if (fromMain > 0 || fromWaiting > 0) {
            log.info("Cleaned sessions: {} from main, {} from waiting", fromMain, fromWaiting);
        }
    }

    /** Удаление загруженных файлов старше 7 дней. Запуск раз в сутки. */
    @Scheduled(cron = "0 0 4 * * *") // 04:00 каждый день
    public void cleanOldUploadedFiles() {
        log.trace("Running old uploaded files cleanup");
        uploadService.deleteFilesOlderThanDays(FILE_STALE_DAYS);
    }
}
