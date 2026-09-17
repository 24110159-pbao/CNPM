package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    long countByUserIdAndIsReadFalse(
            Long userId
    );
}
