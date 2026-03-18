package com.example.tgbot.domain.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Снимок записи SeedancePollingRegistry для сохранения в БД при shutdown.
 */
@Entity
@Table(name = "app_registry_seedance_polling")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRegistrySeedancePolling {

    @Id
    @Column(name = "task_id", length = 256)
    private String taskId;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "last_polled_at")
    private Long lastPolledAt;
}
