package com.example.tgbot.bot;

import com.example.tgbot.data.BotState;
import com.example.tgbot.data.GenModel;
import com.example.tgbot.data.SunoMusicGenre;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.TreeSet;


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