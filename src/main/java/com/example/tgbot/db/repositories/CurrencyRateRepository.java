package com.example.tgbot.db.repositories;

import com.example.tgbot.db.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    Optional<CurrencyRate> findByFromCurrencyAndToCurrency(String from, String to);
}
