package com.example.tgbot.domain.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Коэффициенты скидок для амбассадоров по моделям.
 */
@Getter
@RequiredArgsConstructor
public enum PriceCoefficient {
    AMBASSADOR_KLING_COEFFICIENT(1.1f),
    NON_AMBASSADOR_KLING_COEFFICIENT(1.5f),
    AMBASSADOR_KLING_MOTION_CONTROL_COEFFICIENT(1.5f),
    NON_AMBASSADOR_KLING_MOTION_CONTROL_COEFFICIENT(2f),
    AMBASSADOR_FIXED_PRICE_COEFFICIENT(0.5f);

    private final float coefficient;
}
