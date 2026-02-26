package com.example.tgbot.telegram.buttons;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PaidPackageButton {

    PACKAGE_100("\"100 монет\"", 100, 100, "package_100"),
    PACKAGE_550("\"550 монет\"", 500, 550, "package_550"),
    PACKAGE_1200("\"1200 монет\"", 1000, 1200, "package_1200"),
    PACKAGE_6500("\"6500 монет\"", 5000, 6500, "package_6500");

    private String buttonName;
    private Integer packagePrice;
    private Integer packageAmount;
    private String buttonCallback;

    PaidPackageButton(String buttonName, Integer packagePrice, Integer packageAmount, String buttonCallback) {
        this.buttonName = buttonName;
        this.packagePrice = packagePrice;
        this.packageAmount = packageAmount;
        this.buttonCallback = buttonCallback;
    }

    public static Integer getPackagePriceByName(String externalName) {
        Optional<PaidPackageButton> pack = Arrays.stream(PaidPackageButton.values()).filter(pp -> pp.buttonName.equalsIgnoreCase(externalName)).findFirst();
        return pack.map(PaidPackageButton::getPackageAmount).orElse(null);
    }

    public static PaidPackageButton getPackageByCallback(String cb) {
        Optional<PaidPackageButton> pack = Arrays.stream(PaidPackageButton.values()).filter(pp -> pp.buttonCallback.equalsIgnoreCase(cb)).findFirst();
        return pack.orElse(null);
    }
}
