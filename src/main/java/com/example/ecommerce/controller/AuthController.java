package com.example.ecommerce.controller;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.OtpType;
import com.example.ecommerce.service.OtpService;
import com.example.ecommerce.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/send-register-otp")
    public String sendRegisterOtp(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        if (userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email đã được sử dụng."
            );
            return "redirect:/auth/register";
        }

        otpService.sendOtp(email, OtpType.REGISTER);

        redirectAttributes.addFlashAttribute(
                "success",
                "OTP đã được gửi đến email của bạn."
        );

        return "redirect:/auth/verify-register-otp?email=" + email;
    }

    @GetMapping("/verify-register-otp")
    public String verifyRegisterOtp(
            @RequestParam String email,
            Model model) {

        model.addAttribute("email", email);

        return "auth/verify-otp";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String password,
            @RequestParam String otp,
            RedirectAttributes redirectAttributes) {

        boolean verified =
                otpService.verifyOtp(
                        email,
                        otp,
                        OtpType.REGISTER
                );

        if (!verified) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "OTP không hợp lệ hoặc đã hết hạn."
            );

            return "redirect:/auth/verify-register-otp?email=" + email;
        }

        User user = userService.register(name, email, password);

        if (user == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không thể tạo tài khoản. Email có thể đã tồn tại."
            );

            return "redirect:/auth/verify-register-otp?email=" + email;
        }


        otpService.deleteOtp(email, OtpType.REGISTER);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đăng ký thành công. Vui lòng đăng nhập."
        );

        return "redirect:/auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/send-forgot-otp")
    public String sendForgotPasswordOtp(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        if (!userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email không tồn tại."
            );

            return "redirect:/auth/forgot-password";
        }

        otpService.sendOtp(
                email,
                OtpType.FORGOT_PASSWORD
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "OTP đã được gửi đến email của bạn."
        );

        return "redirect:/auth/reset-password?email=" + email;
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(
            @RequestParam String email,
            Model model) {

        model.addAttribute("email", email);

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {

        boolean verified =
                otpService.verifyOtp(
                        email,
                        otp,
                        OtpType.FORGOT_PASSWORD
                );

        if (!verified) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "OTP không hợp lệ hoặc đã hết hạn."
            );

            return "redirect:/auth/reset-password?email=" + email;
        }

        var user = userService.findByEmail(email);

        userService.updatePassword(
                user.getId(),
                newPassword
        );

        otpService.deleteOtp(
                email,
                OtpType.FORGOT_PASSWORD
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đổi mật khẩu thành công. Vui lòng đăng nhập."
        );

        return "redirect:/auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}
