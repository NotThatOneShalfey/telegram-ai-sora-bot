package com.example.tgbot.data;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PaidPackage {

    PACKAGE_100("\"100 монет\"", 100, 100),
    PACKAGE_550("\"550 монет\"", 500, 550),
    PACKAGE_1200("\"1200 монет\"", 1000, 1200),
    PACKAGE_6500("\"6500 монет\"", 5000, 6500);

    private String packageName;
    private Integer price;
    private Integer amount;

    PaidPackage(String packageName, Integer price) {
        this.packageName = packageName;
        this.price = price;
    }

    PaidPackage(String packageName, Integer price, Integer amount) {
        this.packageName = packageName;
        this.price = price;
        this.amount = amount;
    }

    public static Integer getPackagePriceByName(String externalName) {
        Optional<PaidPackage> pack = Arrays.stream(PaidPackage.values()).filter(pp -> pp.packageName.equalsIgnoreCase(externalName)).findFirst();
        return pack.map(PaidPackage::getAmount).orElse(null);
    }
}
