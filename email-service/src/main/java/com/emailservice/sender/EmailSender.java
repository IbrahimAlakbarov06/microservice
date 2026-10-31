package com.emailservice.sender;

import com.emailservice.event.ItemNotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailSender(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(ItemNotificationEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.recipientEmail());
        message.setSubject("Item notification: " + event.name());
        message.setText("Hello,\n\nItem #" + event.itemId() + " (" + event.name() + ")\n"
                + "Description: " + event.description() + "\n"
                + "Requested at: " + event.createdAt() + "\n");
        mailSender.send(message);
    }
}