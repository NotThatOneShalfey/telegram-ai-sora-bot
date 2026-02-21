package com.example.tgbot.bot;

import com.example.tgbot.data.BotState;
import com.example.tgbot.data.GenModel;
import com.example.tgbot.data.SunoMusicGenre;
import com.example.tgbot.web.callbacks.keiai.KeiAiMusicCallbackResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


@Data
@Slf4j
public class UserSession {
    private BotState state;
    private GenModel model = null;
    private Object selectedFormat = null; // e.g. "16:9" or "9:16"
    private Object payload = null;

    public UserSession() {
        this.state = BotState.INITIAL;
    }

    public UserSession(BotState state) {
        this.state = state;
    }

    public void setSelectedFormat(String selectedFormat) {
        this.selectedFormat = selectedFormat;
    }

    public void setSelectedFormat(SunoMusicGenre selectedFormat) {
        this.selectedFormat = selectedFormat;
    }

}