package com.example.tgbot.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.TreeSet;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {
    private BotState state;
    private String selectedFormat; // e.g. "16:9" or "9:16"
    private final TreeSet<OrderedMessageClass> orderedMessages = new TreeSet<>();

    // Сделал чтобы не держать весь чат в памяти
    // Добавлять нужно только те сообщения, у которых нет кнопки "назад"
    public void putInMessageHistory(SendMessage message) {
        OrderedMessageClass omc = new OrderedMessageClass(message, LocalDateTime.now());
        if (orderedMessages.size() == 5) {
            orderedMessages.remove(orderedMessages.first());
        }
        orderedMessages.add(omc);
    }

    // Идем от последнего
    public SendMessage getLastMessageBeforeCall(Integer unixTime) {
        // Конвертим Unix время в localDateTime
        LocalDateTime ldt = Instant.ofEpochSecond(unixTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Iterator<OrderedMessageClass> iterator = orderedMessages.descendingIterator();
        SendMessage lastMessage = null;
        while (iterator.hasNext()) {
            // Проверка чтобы возвращать последнее сообщение до того, у которого конкретно запросили кнопку "назад"
            if (iterator.next().timeSent.isBefore(ldt)) {
                lastMessage = iterator.next().message;
                break;
            }
        }
        return lastMessage;

    }
}