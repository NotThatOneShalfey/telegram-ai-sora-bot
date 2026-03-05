package com.example.tgbot.db.repositories;

import com.example.tgbot.db.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
    Optional<User> findByUserName(String userName);
    List<User> findByLinkUsedIn(Collection<String> links);
}