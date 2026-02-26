package com.example.tgbot.db.repositories;

import com.example.tgbot.db.OperationsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationsHistoryRepository extends JpaRepository<OperationsHistory, UUID> {
}
