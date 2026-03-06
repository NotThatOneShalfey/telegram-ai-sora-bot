package com.example.tgbot.telegram.button.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PaidPackageEnum {

    PACKAGE_100("\"100 монет\" - 100 ₽", 100, 100),
    PACKAGE_550("\"550 монет\" - 500 ₽", 500, 550),
    PACKAGE_1200("\"1200 монет\" - 1000 ₽", 1000, 1200),
    PACKAGE_6500("\"6500 монет\" - 5000 ₽", 5000, 6500);

    private String buttonName;
    private Integer packagePrice;
    private Integer packageAmount;

    PaidPackageEnum(String buttonName, Integer packagePrice, Integer packageAmount) {
        this.buttonName = buttonName;
        this.packagePrice = packagePrice;
        this.packageAmount = packageAmount;
    }

    public static Integer getPackagePriceByName(String externalName) {
        Optional<PaidPackageEnum> pack = Arrays.stream(PaidPackageEnum.values()).filter(pp -> pp.buttonName.equalsIgnoreCase(externalName)).findFirst();
        return pack.map(PaidPackageEnum::getPackageAmount).orElse(null);
    }
}
