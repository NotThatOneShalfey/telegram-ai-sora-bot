package com.example.tgbot.repository;

import com.example.tgbot.domain.model.PriceRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceRegistryRepository extends JpaRepository<PriceRegistry, Long> {

    List<PriceRegistry> findByModel(String model);

    Optional<PriceRegistry> findByModelAndPriceKey(String model, String priceKey);

    Optional<PriceRegistry> findByModelAndPriceKeyIsNull(String model);
}
