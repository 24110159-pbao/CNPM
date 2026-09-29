package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OtpVerification;
import com.example.ecommerce.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
            String email,
            OtpType type
    );

    Optional<OtpVerification> findTopByEmailAndOtpAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
            String email,
            String otp,
            OtpType type
    );

    void deleteByEmailAndType(
            String email,
            OtpType type
    );
    Optional<OtpVerification> findTopByEmailAndTypeOrderByCreatedAtDesc(
            String email,
            OtpType type
    );
}
