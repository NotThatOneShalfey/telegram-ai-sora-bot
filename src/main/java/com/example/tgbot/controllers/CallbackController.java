package com.example.tgbot.controllers;

import com.example.tgbot.bot.SoraVideoBot;
import com.example.tgbot.service.VideoGenerationService;
import com.example.tgbot.web.CreateTaskResponse;
import com.example.tgbot.web.callbacks.keiai.KeiAiMusicCallbackResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/keiai/callback")
@RequiredArgsConstructor
@Slf4j
public class CallbackController {
    private final JsonMapper jsonMapper = new JsonMapper();

    private final VideoGenerationService videoGenerationService;

    @PostMapping("/music")
    public void handleMusicGenerationCallback(@RequestBody String body) {
        log.debug("handleMusicGenerationCallback method called. Body = {}", body);
        try {
            KeiAiMusicCallbackResponse resp = jsonMapper.readValue(body, KeiAiMusicCallbackResponse.class);
            log.trace("Resp object: {}", resp.toString());
            videoGenerationService.putCallbackResponse(resp);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
