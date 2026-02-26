package com.example.tgbot.telegram.sessions;


import com.example.tgbot.models.enums.GenerationModel;
import lombok.Data;

@Data
public class ChatContext {
    private ChatState state;
    private GenerationModel model = null;

    public ChatContext(ChatState state) {
        this.state = state;
    }
}
