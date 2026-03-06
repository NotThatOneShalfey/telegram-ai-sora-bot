package com.example.tgbot.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "price_registry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pr_id")
    private Long id;

    @Column(name = "pr_model", nullable = false)
    private String model;

    @Column(name = "pr_price_key")
    private String priceKey;

    @Column(name = "pr_base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
}
