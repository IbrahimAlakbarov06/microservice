package com.emailservice.listener;

import com.emailservice.config.RabbitConfig;
import com.emailservice.event.ItemNotificationEvent;
import com.emailservice.sender.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final EmailSender emailSender;

    public NotificationListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handle(ItemNotificationEvent event) {
        log.info("Received event for item {} -> {}", event.itemId(), event.recipientEmail());
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            throw new AmqpRejectAndDontRequeueException("Missing recipient email");
        }
        emailSender.send(event);
        log.info("Email sent to {}", event.recipientEmail());
    }
}