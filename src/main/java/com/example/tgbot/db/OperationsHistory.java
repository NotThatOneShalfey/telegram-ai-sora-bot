package com.example.tgbot.db;

import com.example.tgbot.data.GenerationType;
import com.example.tgbot.data.HistoryOperationType;
import com.example.tgbot.models.enums.GenerationModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operations_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationsHistory {

    @Id
    @Column(name = "oh_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JoinColumn(name = "oh_user_id")
    @OneToOne
    private User userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oh_operation_type")
    private HistoryOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "oh_generation_type")
    private GenerationType generationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "oh_model")
    private GenerationModel model;

    @Column(name = "oh_gen_input")
    private String generationRequestInput;

    @Column(name = "oh_result_urls")
    private String resultUrls;

    @Column(name = "oh_balance_change")
    private Float balanceChange;

    @Column(name = "oh_timestamp")
    private Timestamp operationTimestamp;

    @PrePersist
    protected void onCreate() {
        this.operationTimestamp = Timestamp.from(Instant.now());
    }

}
