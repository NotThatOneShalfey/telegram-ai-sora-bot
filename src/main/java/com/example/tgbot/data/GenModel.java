package com.example.tgbot.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenModel {
    KLING_3_0(150),
    NANO_BANANA(20),
    NANO_BANANA_EDIT(20),
    SORA_2(75);

    private final int price;

}
