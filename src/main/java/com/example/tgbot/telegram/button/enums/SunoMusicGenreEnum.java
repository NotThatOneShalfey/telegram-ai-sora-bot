package com.example.tgbot.telegram.button.enums;

import lombok.Getter;

@Getter
public enum SunoMusicGenreEnum {
    POP("Поп", "\uD83C\uDFB6 Поп"),
    RAP("Рэп / хип-хоп", "\uD83C\uDFA4 Рэп / хип-хоп"),
    DISCO("Диско 90-х годов", "\uD83D\uDC83 Диско 90-х"),
    SHANSON("Шансон", "\uD83D\uDEAC Шансон"),
    ROCK("Рок", "\uD83C\uDFB8 Рок"),
    CLASSIC("Классика", "\uD83C\uDFBB Классика"),
    ELECTRO("Электро", "\uD83D\uDD0A Электро"),
    JAZZ("Джаз", "\uD83C\uDFB7 Джаз"),
    NARODNAYA("Народная", "\uD83E\uDE97 Народная"),
    ACCOUSTIC("Аккустическая", "\uD83E\uDE95 Акустика");


    private final String value;
    private final String buttonText;

    SunoMusicGenreEnum(String value, String buttonText) {
        this.value = value;
        this.buttonText = buttonText;
    }

    public static String getButtonTextByValue(String value) {
        for (SunoMusicGenreEnum smge : SunoMusicGenreEnum.values()) {
            if (smge.getValue().equalsIgnoreCase(value)) {
                return smge.getButtonText();
            }
        }
        return null;
    }
}
