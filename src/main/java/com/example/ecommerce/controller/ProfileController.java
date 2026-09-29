package com.example.ecommerce.controller;

import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String profile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        model.addAttribute(
                "user",
                userService.findById(
                        userDetails.getUserId()
                )
        );

        return "profile/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String address,
            RedirectAttributes redirectAttributes) {

        userService.updateProfile(
                userDetails.getUserId(),
                name,
                phone,
                address
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Cập nhật thông tin thành công."
        );

        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {

        userService.updatePassword(
                userDetails.getUserId(),
                newPassword
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đổi mật khẩu thành công."
        );

        return "redirect:/profile";
    }
}
