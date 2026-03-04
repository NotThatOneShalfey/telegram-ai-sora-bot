package com.example.tgbot.telegram.controllers;

import com.example.tgbot.models.configurations.dto.InterfaceDTORequest;
import com.example.tgbot.models.enums.GenerationModel;
import com.example.tgbot.telegram.TgBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/web")
@RequiredArgsConstructor
@Slf4j
public class WebInterfaceController {
    private final TgBot tgBot;

    @GetMapping("/example/kling")
    public ResponseEntity<String> getKlingOptionsExample(@RequestParam String userName) {
        return new ResponseEntity<>(tgBot.processExampleRequest(userName, GenerationModel.KLING_3_0), HttpStatus.OK);
    }

    @GetMapping("/example/sora2")
    public ResponseEntity<String> getSoraOptionsExample(@RequestParam String userName) {
        return new ResponseEntity<>(tgBot.processExampleRequest(userName, GenerationModel.SORA_2), HttpStatus.OK);
    }

    @GetMapping("/example/suno")
    public ResponseEntity<String> getSunoOptionsExample(@RequestParam String userName) {
        return new ResponseEntity<>(tgBot.processExampleRequest(userName, GenerationModel.KLING_3_0), HttpStatus.OK);
    }

    @GetMapping("/example/nanobanana")
    public ResponseEntity<String> getNanoBananaOptionsExample(@RequestParam String userName) {
        return new ResponseEntity<>(tgBot.processExampleRequest(userName, GenerationModel.KLING_3_0), HttpStatus.OK);
    }

    @PostMapping("/generate")
    public ResponseEntity<HttpStatus> generate(@RequestBody InterfaceDTORequest req) {
        tgBot.onWebInterfaceRequest(req);
        return ResponseEntity.ok().build();
    }
}
