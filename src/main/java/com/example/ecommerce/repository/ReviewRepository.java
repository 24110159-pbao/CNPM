package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdOrderByCreatedAtDesc(
            Long productId,
            Pageable pageable
    );

    Page<Review> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Optional<Review> findByUserIdAndProductIdAndOrderId(
            Long userId,
            Long productId,
            Long orderId
    );

    boolean existsByUserIdAndProductIdAndOrderId(
            Long userId,
            Long productId,
            Long orderId
    );

    long countByProductId(Long productId);
}
