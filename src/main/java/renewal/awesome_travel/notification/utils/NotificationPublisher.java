package renewal.awesome_travel.notification.utils;

import lombok.RequiredArgsConstructor;
// import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import renewal.common.entity.Notification;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    // private final RabbitTemplate rabbitTemplate;

    // public void sendNotification(Notification message) {
    //     rabbitTemplate.convertAndSend("notification.exchange", "notification.routingKey", message);
    // }
}

