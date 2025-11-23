package com.example.tgbot.controllers;

import com.example.tgbot.bot.SoraVideoBot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/telegram")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final SoraVideoBot soraVideoBot;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @PostMapping("/update")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        log.debug("Update received!");
        try {
            log.debug("Json update: {}", jsonMapper.writeValueAsString(update));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return soraVideoBot.onWebhookUpdateReceived(update);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.debug("Test method called");
        return ResponseEntity.ok().body("Hello world!");
    }
}
