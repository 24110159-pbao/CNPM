package com.example.ecommerce.controller;

import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    @ModelAttribute
    public void addUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (userDetails != null) {
            Long userId = userDetails.getUserId();

            model.addAttribute(
                    "unreadCount",
                    notificationService.countUnread(userId)
            );
        }
    }
}