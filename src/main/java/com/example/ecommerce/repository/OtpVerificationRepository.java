package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification>
    findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
            String email,
            String type
    );

    void deleteByEmail(String email);
}
