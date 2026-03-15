package com.example.tgbot.repository;

import com.example.tgbot.domain.model.AppRegistryTaskError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRegistryTaskErrorRepository extends JpaRepository<AppRegistryTaskError, String> {
}
