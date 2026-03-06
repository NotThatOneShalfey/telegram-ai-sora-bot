package com.example.tgbot.telegram.session;


import com.example.tgbot.domain.enums.GenerationModel;
import lombok.Data;

@Data
public class ChatContext {
    private ChatState state;
    private GenerationModel model = null;
    private String contextualMessage = null;

    public ChatContext(ChatState state) {
        this.state = state;
    }
}
