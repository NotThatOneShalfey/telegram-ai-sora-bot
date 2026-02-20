package com.example.tgbot.repository;

import com.example.tgbot.model.ReferralLinks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReferralLinksRepository extends JpaRepository<ReferralLinks, UUID> {

}
