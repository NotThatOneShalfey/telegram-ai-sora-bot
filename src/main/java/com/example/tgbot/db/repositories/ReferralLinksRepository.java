package com.example.tgbot.db.repositories;

import com.example.tgbot.db.ReferralLinks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReferralLinksRepository extends JpaRepository<ReferralLinks, UUID> {

}
