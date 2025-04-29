package renewal.awesome_travel.notification.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 알림 받을 사용자 ID

    private String message;

    private boolean isRead = false; // 읽음 여부 (처음엔 안 읽음)

    private LocalDateTime createdAt;

    public static Notification create(Long userId, String message) {
        Notification notification = new Notification();
        notification.userId = userId;
        notification.message = message;
        notification.createdAt = LocalDateTime.now();
        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}

