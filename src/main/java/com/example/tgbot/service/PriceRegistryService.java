package com.example.tgbot.service;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.domain.model.CurrencyRate;
import com.example.tgbot.domain.model.PriceRegistry;
import com.example.tgbot.domain.model.User;
import com.example.tgbot.domain.value.PriceCoefficient;
import com.example.tgbot.integration.config.IModelRequestOptions;
import com.example.tgbot.integration.config.KlingOptions;
import com.example.tgbot.repository.CurrencyRateRepository;
import com.example.tgbot.repository.PriceRegistryRepository;
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

    private static final String USD = "USD";
    private static final String RUB = "RUB";

    private final PriceRegistryRepository priceRegistryRepository;
    private final CurrencyRateRepository currencyRateRepository;

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

    // Клинг высчитываем от себестоимости
    private int calculateBasePrice(GenerationModel model, IModelRequestOptions options) {
        if (model == GenerationModel.KLING_3_0 && options instanceof KlingOptions kling) {
            return getKlingCostUsd(kling).setScale(0, RoundingMode.HALF_UP).intValue();
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

    /**
     * Возвращает себестоимость в USD. Для Kling — cost_usd * duration; для фиксированных — cost_usd из реестра.
     */
    public BigDecimal getCostUsd(GenerationModel model, IModelRequestOptions options) {
        if (model == GenerationModel.KLING_3_0 && options instanceof KlingOptions kling) {
            return getKlingCostUsd(kling);
        }
        return getFixedCostUsd(model);
    }

    private BigDecimal getKlingCostUsd(KlingOptions kling) {
        String priceKey = buildKlingPriceKey(kling.getMode(), kling.isWithSound());
        Optional<PriceRegistry> reg = priceRegistryRepository.findByModelAndPriceKey(
                GenerationModel.KLING_3_0.name(), priceKey);
        if (reg.isEmpty() || reg.get().getCostUsd() == null) {
            return BigDecimal.ZERO;
        }
        int duration = kling.getDuration() > 0 ? kling.getDuration() : 10;
        return reg.get().getCostUsd().multiply(BigDecimal.valueOf(duration));
    }

    private BigDecimal getFixedCostUsd(GenerationModel model) {
        GenerationModel lookupModel = model == GenerationModel.SORA_2_WITH_IMAGE ? GenerationModel.SORA_2 : model;
        Optional<PriceRegistry> reg = priceRegistryRepository.findByModelAndPriceKeyIsNull(lookupModel.name());
        if (reg.isEmpty()) {
            reg = priceRegistryRepository.findByModel(lookupModel.name()).stream().findFirst();
        }
        if (reg.isEmpty() || reg.get().getCostUsd() == null) {
            return BigDecimal.ZERO;
        }
        return reg.get().getCostUsd();
    }

    /** Себестоимость в рублях: cost_usd * курс USD→RUB. */
    public BigDecimal getCostRub(GenerationModel model, IModelRequestOptions options) {
        BigDecimal costUsd = getCostUsd(model, options);
        if (costUsd == null || costUsd.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currencyRateRepository.findByFromCurrencyAndToCurrency(USD, RUB)
                .map(CurrencyRate::getRate)
                .map(rate -> costUsd.multiply(rate).setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }
}
