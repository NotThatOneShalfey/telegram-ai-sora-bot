package com.example.tgbot.telegram.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationController {
    @PostMapping("/notification")
    public void onPayNotification(@RequestBody String body) {
        log.trace("Payment notification received: {}", body);
    }
}
