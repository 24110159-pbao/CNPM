package com.example.ecommerce.service;

import com.example.ecommerce.entity.OtpVerification;
import com.example.ecommerce.enums.OtpType;
import com.example.ecommerce.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRE_MINUTES = 5;

    @Transactional
    public boolean sendOtp(
            String email,
            OtpType type
    ) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        if (type == null) {
            return false;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        String otp = generateOtp();

        OtpVerification verification =
                otpVerificationRepository
                        .findTopByEmailAndTypeOrderByCreatedAtDesc(
                                normalizedEmail,
                                type
                        )
                        .orElse(null);

        if (verification == null) {

            verification = OtpVerification.builder()
                    .email(normalizedEmail)
                    .otp(otp)
                    .type(type)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(
                            LocalDateTime.now()
                                    .plusMinutes(
                                            OTP_EXPIRE_MINUTES
                                    )
                    )
                    .verified(false)
                    .build();

        } else {

            verification.setOtp(otp);
            verification.setCreatedAt(
                    LocalDateTime.now()
            );
            verification.setExpiresAt(
                    LocalDateTime.now()
                            .plusMinutes(
                                    OTP_EXPIRE_MINUTES
                            )
            );
            verification.setVerified(false);
        }

        otpVerificationRepository.save(verification);

        boolean sent = emailService.sendOtpEmail(
                normalizedEmail,
                otp,
                type
        );

        if (!sent) {
            otpVerificationRepository.delete(verification);
            return false;
        }

        return true;
    }

    @Transactional
    public boolean verifyOtp(
            String email,
            String otp,
            OtpType type
    ) {
        if (email == null
                || email.trim().isEmpty()
                || otp == null
                || otp.trim().isEmpty()
                || type == null) {
            return false;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        OtpVerification verification =
                otpVerificationRepository
                        .findTopByEmailAndTypeOrderByCreatedAtDesc(
                                normalizedEmail,
                                type
                        )
                        .orElse(null);

        if (verification == null) {
            return false;
        }

        if (verification.isVerified()) {
            return false;
        }

        if (verification.getExpiresAt() == null
                || LocalDateTime.now()
                .isAfter(verification.getExpiresAt())) {
            return false;
        }

        if (!verification.getOtp().equals(otp.trim())) {
            return false;
        }

        verification.setVerified(true);

        otpVerificationRepository.save(verification);

        return true;
    }

    public boolean isOtpVerified(
            String email,
            OtpType type
    ) {
        if (email == null
                || email.trim().isEmpty()
                || type == null) {
            return false;
        }

        return otpVerificationRepository
                .findTopByEmailAndTypeOrderByCreatedAtDesc(
                        email.trim().toLowerCase(),
                        type
                )
                .map(OtpVerification::isVerified)
                .orElse(false);
    }

    @Transactional
    public boolean deleteOtp(
            String email,
            OtpType type
    ) {
        if (email == null
                || email.trim().isEmpty()
                || type == null) {
            return false;
        }

        OtpVerification verification =
                otpVerificationRepository
                        .findTopByEmailAndTypeOrderByCreatedAtDesc(
                                email.trim().toLowerCase(),
                                type
                        )
                        .orElse(null);

        if (verification == null) {
            return false;
        }

        otpVerificationRepository.delete(verification);

        return true;
    }

    private String generateOtp() {
        int number =
                secureRandom.nextInt(900000) + 100000;

        return String.valueOf(number);
    }
}
