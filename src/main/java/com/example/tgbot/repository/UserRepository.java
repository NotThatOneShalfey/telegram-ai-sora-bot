package com.example.tgbot.repository;

import com.example.tgbot.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
    Optional<User> findByUserName(String userName);
    List<User> findByLinkUsedIn(Collection<String> links);

    /** Рефералы по ссылкам, исключая амбассадоров (для статистики и накопления прибыли). */
    List<User> findByLinkUsedInAndAmbassadorFalse(Collection<String> links);

    long countByLinkUsedIn(Collection<String> links);

    /** Количество рефералов по ссылкам, исключая амбассадоров. */
    long countByLinkUsedInAndAmbassadorFalse(Collection<String> links);

    long countByLinkUsedInAndCreatedAtBetween(Collection<String> links, Instant from, Instant to);

    /** Новые рефералы за период, исключая амбассадоров. */
    long countByLinkUsedInAndCreatedAtBetweenAndAmbassadorFalse(Collection<String> links, Instant from, Instant to);

    List<User> findByAmbassadorTrue();
}
