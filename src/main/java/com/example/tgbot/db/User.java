package com.example.tgbot.db;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;

    @Column(name = "balance", nullable = false)
    private Integer balance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "bonus_received", nullable = false)
    private boolean bonusReceived;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "is_ambassador")
    private boolean ambassador;

    @Column(name = "referral_link_used")
    private String linkUsed;

    @Column(name = "balance_hold")
    private Integer balanceHold;

    @Column(name = "ambassador_profit_coefficient", precision = 5, scale = 4)
    private BigDecimal ambassadorProfitCoefficient;

    @Column(name = "ambassador_profit_total", precision = 14, scale = 2)
    private BigDecimal ambassadorProfitTotal;

    @Column(name = "last_accumulated_period_end")
    private LocalDate lastAccumulatedPeriodEnd;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}