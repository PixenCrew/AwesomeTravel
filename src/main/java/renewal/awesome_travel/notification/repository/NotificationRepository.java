package renewal.awesome_travel.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.notification.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
}

