package com.example.tgbot.controllers;

import com.example.tgbot.web.CreateTaskResponse;
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

    @PostMapping("/music")
    public void handleMusicGenerationCallback(@RequestBody String body) {
        log.debug("handleMusicGenerationCallback method called. Body = {}", body);
        try {
            CreateTaskResponse resp = jsonMapper.readValue(body, CreateTaskResponse.class);
            log.trace("Resp object: {}", resp.toString());
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
