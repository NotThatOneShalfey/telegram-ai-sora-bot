package com.example.tgbot.repository;

import com.example.tgbot.domain.model.AppRegistryTaskResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRegistryTaskResultRepository extends JpaRepository<AppRegistryTaskResult, String> {
}
