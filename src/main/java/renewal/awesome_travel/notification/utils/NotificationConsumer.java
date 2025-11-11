package renewal.awesome_travel.notification.utils;

// import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    // private final NotificationRepository notificationRepository;

    // @RabbitListener(queues = "notification.queue")
    // public void handleNotification(NotificationMessage message) {
    // notificationRepository.save(Notification.create(
    // message.getUserId(),
    // message.getMessage()
    // ));
    // }
}
