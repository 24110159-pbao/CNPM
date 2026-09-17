package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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

    long countByStatus(OrderStatus status);

    long countByUserId(Long userId);
}
