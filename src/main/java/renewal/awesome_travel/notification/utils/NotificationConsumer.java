package renewal.awesome_travel.notification.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import renewal.awesome_travel.notification.entity.Notification;
import renewal.awesome_travel.notification.entity.NotificationMessage;
import renewal.awesome_travel.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = "notification.queue")
    public void handleNotification(NotificationMessage message) {
        notificationRepository.save(Notification.create(
                message.getUserId(),
                message.getMessage()
        ));
    }
}

