package com.example.tgbot.controller;

import com.example.tgbot.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Контроллер административных операций.
 */
@RestController
@RequestMapping("v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    /**
     * Устанавливает статус амбассадора пользователю.
     * Обновляет users: is_ambassador=true, ambassador_profit_coefficient=0.2.
     * Добавляет запись в referral_links: rl_link, rl_created_by.
     *
     * @param userName     userName пользователя
     * @param referralLink реферальная ссылка
     */
    @PostMapping("/set-ambassador")
    public ResponseEntity<?> setAmbassadorStatus(
            @RequestParam("userName") String userName,
            @RequestParam("referralLink") String referralLink) {
        boolean success = adminService.setAmbassadorStatus(userName, referralLink);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Ambassador status set"));
        }
        return ResponseEntity.notFound().build();
    }
}
