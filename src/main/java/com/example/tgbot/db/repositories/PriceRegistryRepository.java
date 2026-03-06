package com.example.tgbot.db.repositories;

import com.example.tgbot.db.PriceRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceRegistryRepository extends JpaRepository<PriceRegistry, Long> {

    List<PriceRegistry> findByModel(String model);

    Optional<PriceRegistry> findByModelAndPriceKey(String model, String priceKey);

    Optional<PriceRegistry> findByModelAndPriceKeyIsNull(String model);
}
