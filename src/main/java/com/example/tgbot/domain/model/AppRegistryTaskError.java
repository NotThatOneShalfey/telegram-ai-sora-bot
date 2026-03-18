package com.example.tgbot.domain.model;

import com.example.tgbot.domain.value.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

/**
 * Снимок записи TaskErrorRegistry для сохранения в БД при shutdown.
 */
@Entity
@Table(name = "app_registry_task_error")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRegistryTaskError {

    @Id
    @Column(name = "task_id", length = 256)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_code", nullable = false, length = 32)
    private ErrorCode errorCode;
}
