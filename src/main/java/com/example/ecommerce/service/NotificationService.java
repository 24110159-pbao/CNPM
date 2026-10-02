package com.example.ecommerce.service;

import com.example.ecommerce.entity.Notification;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.NotificationType;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.repository.NotificationRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Page<Notification> getUserNotifications(
            Long userId,
            Pageable pageable
    ) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(
                        userId,
                        pageable
                );
    }

    public Page<Notification> getUnreadNotifications(
            Long userId,
            Pageable pageable
    ) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                        userId,
                        pageable
                );
    }

    public long countUnread(Long userId) {
        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Transactional
    public Notification createNotification(
            Long userId,
            String title,
            String message,
            NotificationType type
    ) {
        if (userId == null) {
            return null;
        }

        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        if (type == null) {
            return null;
        }

        User user = userRepository
                .findById(userId)
                .orElse(null);

        if (user == null) {
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title.trim())
                .message(message.trim())
                .type(type)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public void notifyManagers(
            String title,
            String message,
            NotificationType type
    ) {
        if (title == null || title.isBlank()
                || message == null || message.isBlank()
                || type == null) {
            return;
        }

        userRepository.findByRole(Role.MANAGER, Pageable.unpaged())
                .forEach(manager -> notificationRepository.save(
                        Notification.builder()
                                .user(manager)
                                .title(title.trim())
                                .message(message.trim())
                                .type(type)
                                .isRead(false)
                                .build()
                ));
    }

    @Transactional
    public boolean markAsRead(
            Long notificationId,
            Long userId
    ) {
        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElse(null);

        if (notification == null) {
            return false;
        }

        if (!notification.getUser().getId().equals(userId)) {
            return false;
        }

        notification.setIsRead(true);

        notificationRepository.save(notification);

        return true;
    }

    @Transactional
    public boolean markAllAsRead(Long userId) {
        Page<Notification> notifications =
                notificationRepository
                        .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                                userId,
                                Pageable.unpaged()
                        );

        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }

        notificationRepository.saveAll(notifications.getContent());

        return true;
    }
}
