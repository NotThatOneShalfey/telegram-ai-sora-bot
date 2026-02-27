package com.example.tgbot.telegram.buttons.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum SunoMusicGenreEnum {
    POP("Поп", "pop-genre", "\uD83C\uDFB6 Поп"),
    RAP("Рэп / хип-хоп", "rap-genre", "\uD83C\uDFA4 Рэп / хип-хоп"),
    DISCO("Диско 90-х годов", "disco-genre", "\uD83D\uDC83 Диско 90-х"),
    SHANSON("Шансон", "shanson-genre", "\uD83D\uDEAC Шансон"),
    ROCK("Рок", "rock-genre", "\uD83C\uDFB8 Рок"),
    CLASSIC("Классика", "classic-genre", "\uD83C\uDFBB Классика"),
    ELECTRO("Электро", "electro-genre", "\uD83D\uDD0A Электро"),
    JAZZ("Джаз", "jazz-genre", "\uD83C\uDFB7 Джаз"),
    NARODNAYA("Народная", "narodnaya-genre", "\uD83E\uDE97 Народная"),
    ACCOUSTIC("Аккустическая", "acoustic-genre", "\uD83E\uDE95 Акустика");


    private final String buttonValueForOptions;
    private final String buttonCallback;
    private final String buttonText;

    SunoMusicGenreEnum(String buttonValueForOptions, String buttonCallback, String buttonText) {
        this.buttonValueForOptions = buttonValueForOptions;
        this.buttonCallback = buttonCallback;
        this.buttonText = buttonText;
    }

    public static SunoMusicGenreEnum getPackageByCallback(String cb) {
        Optional<SunoMusicGenreEnum> pack = Arrays.stream(SunoMusicGenreEnum.values()).filter(pp -> pp.buttonCallback.equalsIgnoreCase(cb)).findFirst();
        return pack.orElse(null);
    }
}
