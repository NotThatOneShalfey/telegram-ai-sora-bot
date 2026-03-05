package com.example.tgbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramAiSoraBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(TelegramAiSoraBotApplication.class, args);
    }
}