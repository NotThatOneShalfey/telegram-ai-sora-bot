package com.example.tgbot.data;

import lombok.Getter;

@Getter
public enum SunoMusicGenre {
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


    private final String localDesc;
    private final String callback;
    private final String buttonDescription;

    SunoMusicGenre(String localDesc, String callback, String buttonDescription) {
        this.localDesc = localDesc;
        this.callback = callback;
        this.buttonDescription = buttonDescription;
    }
}
