package com.example.ecommerce.service;

import com.example.ecommerce.entity.DiscountCode;
import com.example.ecommerce.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;

    public Page<DiscountCode> getAllDiscountCodes(
            Pageable pageable
    ) {
        return discountCodeRepository.findAll(pageable);
    }

    public DiscountCode findById(Long id) {
        return discountCodeRepository.findById(id).orElse(null);
    }

    public DiscountCode findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        return discountCodeRepository
                .findByCodeIgnoreCase(code.trim())
                .orElse(null);
    }

    public boolean isValid(
            DiscountCode discountCode
    ) {
        if (discountCode == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (discountCode.getQuantity() <= 0) {
            return false;
        }

        return !now.isBefore(discountCode.getStartAt())
                && !now.isAfter(discountCode.getEndAt());
    }

    public BigDecimal calculateDiscount(
            String code,
            BigDecimal subtotal
    ) {
        if (subtotal == null
                || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (code == null || code.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        DiscountCode discountCode = findByCode(code);

        if (!isValid(discountCode)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountPercent =
                BigDecimal.valueOf(
                        discountCode.getDiscountValue()
                );

        return subtotal
                .multiply(discountPercent)
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    @Transactional
    public DiscountCode createDiscount(
            String code,
            Integer discountValue,
            Integer quantity,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        if (discountValue == null
                || discountValue <= 0
                || discountValue > 100) {
            return null;
        }

        if (quantity == null || quantity < 0) {
            return null;
        }

        if (startAt == null || endAt == null) {
            return null;
        }

        if (!startAt.isBefore(endAt)) {
            return null;
        }

        String normalizedCode =
                code.trim().toUpperCase();

        if (discountCodeRepository
                .existsByCodeIgnoreCase(normalizedCode)) {
            return null;
        }

        DiscountCode discountCode = DiscountCode.builder()
                .code(normalizedCode)
                .discountValue(discountValue)
                .quantity(quantity)
                .startAt(startAt)
                .endAt(endAt)
                .build();

        return discountCodeRepository.save(discountCode);
    }

    @Transactional
    public boolean updateDiscount(
            Long id,
            String code,
            Integer discountValue,
            Integer quantity,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        DiscountCode discountCode =
                discountCodeRepository
                        .findById(id)
                        .orElse(null);

        if (discountCode == null) {
            return false;
        }

        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        if (discountValue == null
                || discountValue <= 0
                || discountValue > 100) {
            return false;
        }

        if (quantity == null || quantity < 0) {
            return false;
        }

        if (startAt == null || endAt == null) {
            return false;
        }

        if (!startAt.isBefore(endAt)) {
            return false;
        }

        String normalizedCode =
                code.trim().toUpperCase();

        DiscountCode existing =
                discountCodeRepository
                        .findByCodeIgnoreCase(normalizedCode)
                        .orElse(null);

        if (existing != null
                && !existing.getId().equals(id)) {
            return false;
        }

        discountCode.setCode(normalizedCode);
        discountCode.setDiscountValue(discountValue);
        discountCode.setQuantity(quantity);
        discountCode.setStartAt(startAt);
        discountCode.setEndAt(endAt);

        discountCodeRepository.save(discountCode);

        return true;
    }

    @Transactional
    public boolean deleteDiscount(Long id) {
        DiscountCode discountCode =
                discountCodeRepository
                        .findById(id)
                        .orElse(null);

        if (discountCode == null) {
            return false;
        }

        discountCodeRepository.delete(discountCode);

        return true;
    }

    @Transactional
    public boolean decreaseQuantity(Long id) {
        DiscountCode discountCode =
                discountCodeRepository
                        .findById(id)
                        .orElse(null);

        if (discountCode == null) {
            return false;
        }

        if (!isValid(discountCode)) {
            return false;
        }

        if (discountCode.getQuantity() <= 0) {
            return false;
        }

        discountCode.setQuantity(
                discountCode.getQuantity() - 1
        );

        discountCodeRepository.save(discountCode);

        return true;
    }
}
