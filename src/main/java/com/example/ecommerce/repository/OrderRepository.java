package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            OrderStatus status,
            Pageable pageable
    );

    Page<Order> findByStatusOrderByCreatedAtDesc(
            OrderStatus status,
            Pageable pageable
    );

    Page<Order> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    Optional<Order> findByIdAndUserId(
            Long orderId,
            Long userId
    );

    // --- Lọc theo ngày đặt ---
    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<Order> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    long countByStatus(OrderStatus status);

    long countByUserId(Long userId);

    long countByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    long countByStatusAndCreatedAtBetween(
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumFinalAmountByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    BigDecimal sumFinalAmountByStatusAndCreatedAtBetween(
            @Param("status") OrderStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
