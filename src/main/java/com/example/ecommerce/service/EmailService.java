package com.example.ecommerce.service;

import com.example.ecommerce.enums.OtpType;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public boolean sendOtpEmail(
            String email,
            String otp,
            OtpType type
    ) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        if (otp == null || otp.trim().isEmpty()) {
            return false;
        }

        if (type == null) {
            return false;
        }

        String subject;
        String message;

        if (type == OtpType.REGISTER) {

            subject = "Xác thực đăng ký tài khoản";

            message = """
                    Xin chào,

                    Mã OTP xác thực đăng ký tài khoản của bạn là:

                    %s

                    Mã OTP có hiệu lực trong 5 phút.

                    Nếu bạn không thực hiện đăng ký tài khoản,
                    vui lòng bỏ qua email này.

                    Trân trọng,
                    Ecommerce
                    """.formatted(otp);

        } else if (type == OtpType.FORGOT_PASSWORD) {

            subject = "Mã OTP đặt lại mật khẩu";

            message = """
                    Xin chào,

                    Mã OTP để đặt lại mật khẩu của bạn là:

                    %s

                    Mã OTP có hiệu lực trong 5 phút.

                    Nếu bạn không yêu cầu đặt lại mật khẩu,
                    vui lòng bỏ qua email này.

                    Trân trọng,
                    Ecommerce
                    """.formatted(otp);

        } else {
            return false;
        }

        try {
            SimpleMailMessage mail =
                    new SimpleMailMessage();

            mail.setTo(email.trim());
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
