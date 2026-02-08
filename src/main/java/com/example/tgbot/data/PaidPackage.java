package com.example.tgbot.data;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PaidPackage {

    PACKAGE_1("\"1 видео\"", 81, 1),
    PACKAGE_5("\"5 генераций\"", 350, 5),
    PACKAGE_50("\"50 генераций\"", 3000, 50);

    private String packageName;
    private Integer price;
    private Integer generationAmount;

    PaidPackage(String packageName, Integer price) {
        this.packageName = packageName;
        this.price = price;
    }

    PaidPackage(String packageName, Integer price, Integer generationAmount) {
        this.packageName = packageName;
        this.price = price;
        this.generationAmount = generationAmount;
    }

    public static Integer getPackagePriceByName(String externalName) {
        Optional<PaidPackage> pack = Arrays.stream(PaidPackage.values()).filter(pp -> pp.packageName.equalsIgnoreCase(externalName)).findFirst();
        return pack.map(PaidPackage::getGenerationAmount).orElse(null);
    }
}
