package com.example.tgbot.repository;

import com.example.tgbot.domain.model.AppRegistryWaitingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRegistryWaitingSessionRepository extends JpaRepository<AppRegistryWaitingSession, String> {
}
