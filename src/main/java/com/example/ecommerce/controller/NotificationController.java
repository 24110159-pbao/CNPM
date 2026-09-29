package com.example.ecommerce.controller;

import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String notifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Long userId = userDetails.getUserId();

        model.addAttribute(
                "notifications",
                notificationService.getUserNotifications(
                        userId,
                        pageable
                )
        );

        model.addAttribute(
                "unreadCount",
                notificationService.countUnread(userId)
        );

        return "profile/notifications";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        notificationService.markAsRead(
                id,
                userDetails.getUserId()
        );

        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        notificationService.markAllAsRead(
                userDetails.getUserId()
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã đánh dấu tất cả thông báo là đã đọc."
        );

        return "redirect:/notifications";
    }
}
