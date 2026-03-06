package com.example.tgbot.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "currency_rate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cr_id")
    private Long id;

    @Column(name = "cr_from_currency", nullable = false, length = 8)
    private String fromCurrency;

    @Column(name = "cr_to_currency", nullable = false, length = 8)
    private String toCurrency;

    @Column(name = "cr_rate", nullable = false, precision = 12, scale = 4)
    private BigDecimal rate;
}
