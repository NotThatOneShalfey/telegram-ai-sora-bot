package com.example.tgbot.bot;


import lombok.NonNull;
import lombok.ToString;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;

public class OrderedMessageClass implements Comparable<OrderedMessageClass> {
    LocalDateTime timeSent;
    SendMessage message;

    public OrderedMessageClass(SendMessage message, LocalDateTime dateTimeSent) {
        timeSent = dateTimeSent;
        this.message = message;
    }

    @Override
    public int compareTo(@NonNull OrderedMessageClass that) {
        if (this.equals(that)) {
            return 0;
        }
        if (this.timeSent.isAfter(that.timeSent)) {
            return 1;
        }
        if (this.timeSent.isEqual(that.timeSent)) {
            return 0;
        }
        if (this.timeSent.isBefore(that.timeSent)) {
            return -1;
        }
        return 0;
    }
    @Override
    public String toString() {
        return "OrderedMessageClass(" +
                "timeSent=" + timeSent + "," +
                "message=" + message.getText() +
                ")";
    }
}
