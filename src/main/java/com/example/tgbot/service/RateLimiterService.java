package com.example.tgbot.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RateLimiterService {

    private final int capacity;

    private final Duration refillPeriod;

    private final Map<Long, UserRateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimiterService() {
        this(5, Duration.ofMinutes(1));
    }

    public RateLimiterService(int capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriod = refillPeriod;
    }

    public boolean tryConsume(long userId) {
        UserRateLimiter limiter = limiters.computeIfAbsent(userId,
                id -> new UserRateLimiter(capacity, refillPeriod));
        return limiter.tryConsume();
    }

    private static final class UserRateLimiter {
        private final int capacity;
        private final Duration refillPeriod;
        private int tokens;
        private Instant lastRefill;

        UserRateLimiter(int capacity, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillPeriod = refillPeriod;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        synchronized boolean tryConsume() {
            Instant now = Instant.now();
            long millisSinceRefill = Duration.between(lastRefill, now).toMillis();
            long refillIntervalMillis = refillPeriod.toMillis();
            if (millisSinceRefill > 0) {
                long periods = millisSinceRefill / refillIntervalMillis;
                if (periods > 0) {
                    // restore tokens proportionally to the number of periods elapsed
                    int newTokens = (int) (periods * capacity);
                    tokens = Math.min(capacity, tokens + newTokens);
                    // update last refill time
                    lastRefill = lastRefill.plus(refillPeriod.multipliedBy(periods));
                }
            }
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }
    }
}