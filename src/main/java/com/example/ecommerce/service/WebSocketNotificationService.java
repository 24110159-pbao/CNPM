package com.example.ecommerce.service;

import com.example.ecommerce.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(
            Long userId,
            Notification notification
    ) {
        if (userId == null
                || notification == null) {
            return;
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "id",
                notification.getId()
        );

        data.put(
                "title",
                notification.getTitle()
        );

        data.put(
                "message",
                notification.getMessage()
        );

        data.put(
                "type",
                notification.getType()
        );

        data.put(
                "isRead",
                notification.getIsRead()
        );

        data.put(
                "createdAt",
                notification.getCreatedAt()
        );

        messagingTemplate.convertAndSend(
                "/topic/user/" + userId,
                (Object) data
        );

    }

    public void sendOrderNotification(
            Long userId,
            Notification notification
    ) {
        sendToUser(
                userId,
                notification
        );
    }
}
