package com.example.tgbot.domain.model;

import com.example.tgbot.domain.enums.GenerationModel;
import jakarta.persistence.*;
import lombok.*;

/**
 * Снимок записи TaskResultRegistry для сохранения в БД при shutdown.
 */
@Entity
@Table(name = "app_registry_task_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRegistryTaskResult {

    @Id
    @Column(name = "task_id", length = 256)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "model", nullable = false, length = 64)
    private GenerationModel model;

    @Column(name = "options_json", columnDefinition = "text")
    private String optionsJson;

    @Column(name = "result_items_json", columnDefinition = "text")
    private String resultItemsJson;

    @Column(name = "balance_change", nullable = false)
    private int balanceChange;
}
