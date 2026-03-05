package com.example.tgbot.db.repositories;

import com.example.tgbot.db.OperationsHistory;
import com.example.tgbot.db.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OperationsHistoryRepository extends JpaRepository<OperationsHistory, UUID> {

    List<OperationsHistory> findByUserIdOrderByOperationTimestampDesc(User user);
}
