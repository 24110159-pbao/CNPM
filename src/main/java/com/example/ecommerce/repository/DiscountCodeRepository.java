package com.example.ecommerce.repository;

import com.example.ecommerce.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeRepository
        extends JpaRepository<DiscountCode, Long> {

    Optional<DiscountCode> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    Optional<DiscountCode> findByCodeIgnoreCaseAndActiveTrue(
            String code
    );
}
