package com.example.tgbot.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.TreeSet;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class UserSession {
    private BotState state;
    private String selectedFormat; // e.g. "16:9" or "9:16"
    private final TreeSet<OrderedMessageClass> messageHistory = new TreeSet<>();

    // Сделал чтобы не держать весь чат в памяти
    // Добавлять нужно только те сообщения, у которых нет кнопки "назад"
    public void putMessageHistory(SendMessage message) {
        OrderedMessageClass omc = new OrderedMessageClass(message, LocalDateTime.now());
        if (messageHistory.size() == 5) {
            messageHistory.remove(messageHistory.first());
        }
        messageHistory.add(omc);
    }

    public SendMessage getLastMessageBeforeCall() {
        if (messageHistory.isEmpty()) {
            return null;
        }
        OrderedMessageClass lastMessage = messageHistory.last();
        OrderedMessageClass exactElement;
        if (messageHistory.size() < 2) {
            exactElement = lastMessage;
        } else {
            exactElement = messageHistory.lower(lastMessage);
        }
        return exactElement.getMessage();
    }
}