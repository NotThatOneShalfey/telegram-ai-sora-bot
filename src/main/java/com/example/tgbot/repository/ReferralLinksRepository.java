package com.example.tgbot.repository;

import com.example.tgbot.domain.model.ReferralLinks;
import com.example.tgbot.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferralLinksRepository extends JpaRepository<ReferralLinks, UUID> {
    Optional<ReferralLinks> findByLink(String link);

    @Query("SELECT rl FROM ReferralLinks rl WHERE rl.created_by = :creator")
    List<ReferralLinks> findByCreator(@Param("creator") User creator);
}
