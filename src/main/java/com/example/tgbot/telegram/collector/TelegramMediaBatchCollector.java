package com.example.tgbot.telegram.collector;

import com.example.tgbot.telegram.handler.MessageHandler;
import com.example.tgbot.telegram.session.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.util.*;
import java.util.concurrent.*;

/**
 * Собирает сообщения альбома (одинаковый media_group_id) и передаёт prompt + fileIds одним батчем.
 */
@Component
@Slf4j
public class TelegramMediaBatchCollector {

    private static final long FLUSH_DELAY_MS = 800;

    private final MessageHandler messageHandler;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, PendingBatch> batches = new ConcurrentHashMap<>();

    public TelegramMediaBatchCollector(@Lazy MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /** @return true если добавлено в батч (не обрабатывать), false — обработать сразу */
    public boolean offer(Message message, UserSession session) {
        if (message.getMediaGroupId() == null || message.getMediaGroupId().isBlank()) return false;

        String key = message.getChatId() + ":" + message.getMediaGroupId();
        batches.compute(key, (k, batch) -> {
            PendingBatch b = batch != null ? batch : new PendingBatch(session);
            b.add(message);
            b.schedule(scheduler, FLUSH_DELAY_MS, () -> flush(key));
            return b;
        });
        return true;
    }

    private void flush(String key) {
        PendingBatch batch = batches.remove(key);
        if (batch == null || batch.fileIds.isEmpty()) return;
        log.trace("Flushing media batch: {} fileIds for chatId={}", batch.fileIds.size(), batch.session.getChatId());
        messageHandler.handleMediaGroupBatch(batch.prompt, batch.fileIds, batch.session);
    }

    private static class PendingBatch {
        final UserSession session;
        String prompt;
        final List<String> fileIds = new ArrayList<>();
        ScheduledFuture<?> task;

        PendingBatch(UserSession session) { this.session = session; }

        synchronized void add(Message m) {
            if (m.getCaption() != null && !m.getCaption().isBlank()) prompt = m.getCaption();
            if (m.hasPhoto()) {
                m.getPhoto().stream().max(Comparator.comparingInt(PhotoSize::getFileSize))
                        .ifPresent(p -> fileIds.add(p.getFileId()));
            } else if (m.hasDocument()) {
                fileIds.add(m.getDocument().getFileId());
            }
        }

        synchronized void schedule(ScheduledExecutorService s, long delayMs, Runnable onFlush) {
            if (task != null) task.cancel(false);
            task = s.schedule(() -> { try { onFlush.run(); } catch (Exception e) { log.error("Flush error", e); } }, delayMs, TimeUnit.MILLISECONDS);
        }
    }
}
