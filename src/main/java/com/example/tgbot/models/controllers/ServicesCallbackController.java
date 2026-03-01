package com.example.tgbot.models.controllers;

import com.example.tgbot.models.data.RecordInfoResponse;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.handlers.CallbackHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callbacks")
@RequiredArgsConstructor
@Slf4j
public class ServicesCallbackController {

    ObjectMapper jsonMapper = new JsonMapper();
    private final CallbackHandler callbackHandler;

    @PostMapping("/suno-v5")
    public void handleSunoCallback(@RequestBody String body) {
        log.debug("handleSunoCallback method called!");
        try {
            RecordInfoResponse resp = jsonMapper.readValue(body, RecordInfoResponse.class);
            log.trace("Resp object: {}", resp.toString());
            callbackHandler.handleApiCallback(resp, GenerationModel.SUNO_V5);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/sora2")
    public void handleSoraCallback(@RequestBody String body) {
        log.debug("handleSoraCallback method called!");
        try {
            RecordInfoResponse resp = jsonMapper.readValue(body, RecordInfoResponse.class);
            log.trace("Resp object: {}", resp.toString());
            callbackHandler.handleApiCallback(resp, GenerationModel.SORA_2);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/kling-3-0")
    public void handleKlingCallback(@RequestBody String body) {
        log.debug("handleKlingCallback method called!");
        try {
            RecordInfoResponse resp = jsonMapper.readValue(body, RecordInfoResponse.class);
            log.trace("Resp object: {}", resp.toString());
            callbackHandler.handleApiCallback(resp, GenerationModel.KLING_3_0);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/nano-banana-pro")
    public void handleNanoBananaCallback(@RequestBody String body) {
        log.debug("handleNanoBananaCallback method called!");
        try {
            log.trace("Resp object body: {}", body);
            RecordInfoResponse resp = jsonMapper.readValue(body, RecordInfoResponse.class);
            log.trace("Resp object: {}", resp.toString());
            callbackHandler.handleApiCallback(resp, GenerationModel.NANO_BANANA_PRO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
