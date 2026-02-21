package com.example.tgbot.repository;

import com.example.tgbot.model.OperationsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationsHistoryRepository extends JpaRepository<OperationsHistory, UUID> {
}
