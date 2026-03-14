package com.example.tgbot.controller;

import com.example.tgbot.domain.enums.GenerationModel;
import com.example.tgbot.integration.kieai.RecordInfoResponse;
import com.example.tgbot.integration.kieai.SunoInfoResponse;
import com.example.tgbot.telegram.handler.CallbackHandler;
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
            SunoInfoResponse resp = jsonMapper.readValue(body, SunoInfoResponse.class);
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

    @PostMapping("/kling-3-motion-control")
    public void handleKlingMotionControlCallback(@RequestBody String body) {
        log.debug("handleKlingMotionControlCallback method called!");
        try {
            RecordInfoResponse resp = jsonMapper.readValue(body, RecordInfoResponse.class);
            log.trace("Resp object: {}", resp.toString());
            callbackHandler.handleApiCallback(resp, GenerationModel.KLING_3_MOTION_CONTROL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/elevenlabs-v3")
    public void handleElevenLabsCallback(@RequestBody String body) {
        log.debug("handleElevenLabsCallback method called!");
        try {
            RecordInfoResponse resp = jsonMapper.readValue(body, RecordInfoResponse.class);
            log.trace("Resp object: {}", resp.toString());
            callbackHandler.handleApiCallback(resp, GenerationModel.ELEVENLABS_V3);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/nano-banana-pro")
    public void handleNanoBananaCallback(@RequestBody String body) {
        log.debug("handleNanoBananaCallback method called!");
        try {
            RecordInfoResponse resp = jsonMapper.readValue(body, RecordInfoResponse.class);
            log.trace("Resp object: {}", resp.toString());
            callbackHandler.handleApiCallback(resp, GenerationModel.NANO_BANANA_PRO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
