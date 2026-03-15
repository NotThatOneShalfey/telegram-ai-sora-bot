package com.example.tgbot.domain.model;

import com.example.tgbot.domain.enums.GenerationModel;
import jakarta.persistence.*;
import lombok.*;

/**
 * Снимок записи SessionRegistry (ожидающая сессия) для сохранения в БД при shutdown.
 */
@Entity
@Table(name = "app_registry_waiting_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRegistryWaitingSession {

    @Id
    @Column(name = "task_id", length = 256)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source", nullable = false, length = 16)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "model", nullable = false, length = 64)
    private GenerationModel model;

    @Column(name = "options_json", columnDefinition = "text")
    private String optionsJson;
}
