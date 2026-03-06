package com.example.tgbot.service;

import com.example.tgbot.data.PriceCoefficient;
import com.example.tgbot.db.PriceRegistry;
import com.example.tgbot.db.User;
import com.example.tgbot.db.repositories.PriceRegistryRepository;
import com.example.tgbot.models.configurations.IModelRequestOptions;
import com.example.tgbot.models.configurations.KlingOptions;
import com.example.tgbot.models.enums.GenerationModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Расчёт итоговой цены генерации на основе реестра цен в БД.
 * Учитывает тип модели, параметры (для Kling) и коэффициенты из PriceCoefficient.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceRegistryService {

    private final PriceRegistryRepository priceRegistryRepository;

    /**
     * Рассчитывает итоговую цену (с учётом коэффициента по модели и статусу амбассадора) для отображения, холда и списания.
     */
    public int calculatePrice(GenerationModel model, IModelRequestOptions options, User user) {
        int basePrice = calculateBasePrice(model, options);
        float coefficient = getCoefficient(model, user);
        return Math.round(basePrice * coefficient);
    }

    private float getCoefficient(GenerationModel model, User user) {
        boolean ambassador = user != null && user.isAmbassador();
        if (model == GenerationModel.KLING_3_0) {
            return ambassador
                    ? PriceCoefficient.AMBASSADOR_KLING_COEFFICIENT.getCoefficient()
                    : PriceCoefficient.NON_AMBASSADOR_KLING_COEFFICIENT.getCoefficient();
        }
        return ambassador
                ? PriceCoefficient.AMBASSADOR_FIXED_PRICE_COEFFICIENT.getCoefficient()
                : 1f;
    }

    private int calculateBasePrice(GenerationModel model, IModelRequestOptions options) {
        if (model == GenerationModel.KLING_3_0 && options instanceof KlingOptions kling) {
            return calculateKlingPrice(kling);
        }
        return getFixedPrice(model);
    }

    private int calculateKlingPrice(KlingOptions kling) {
        String priceKey = buildKlingPriceKey(kling.getMode(), kling.isWithSound());
        Optional<PriceRegistry> reg = priceRegistryRepository.findByModelAndPriceKey(
                GenerationModel.KLING_3_0.name(), priceKey);
        if (reg.isEmpty()) {
            log.warn("No price for Kling key={}, using fallback", priceKey);
            return 77; // fallback ≈ base 7.66 * duration 10
        }
        PriceRegistry r = reg.get();
        BigDecimal base = r.getBasePrice();
        int duration = kling.getDuration() > 0 ? kling.getDuration() : 10;
        return base.multiply(BigDecimal.valueOf(duration)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private static String buildKlingPriceKey(String mode, boolean withSound) {
        String m = (mode != null && mode.equalsIgnoreCase("pro")) ? "pro" : "std";
        return "{\"mode\":\"" + m + "\",\"with_sound\":" + withSound + "}";
    }

    private int getFixedPrice(GenerationModel model) {
        GenerationModel lookupModel = model == GenerationModel.SORA_2_WITH_IMAGE ? GenerationModel.SORA_2 : model;
        Optional<PriceRegistry> reg = priceRegistryRepository.findByModelAndPriceKeyIsNull(lookupModel.name());
        if (reg.isEmpty()) {
            reg = priceRegistryRepository.findByModel(lookupModel.name()).stream().findFirst();
        }
        if (reg.isEmpty()) {
            log.warn("No price for model={}, using fallback 20", model);
            return 20;
        }
        return reg.get().getBasePrice().setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
