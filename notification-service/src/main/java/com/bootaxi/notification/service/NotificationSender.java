package com.bootaxi.notification.service;

import com.bootaxi.notification.domain.NotificationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    public void send(NotificationTask task) throws InterruptedException {
        Thread.sleep(400);
        if (task.getMessage() != null && task.getMessage().contains("[FAIL]")) {
            throw new IllegalStateException("Simulated notification failure");
        }
        log.info("Notification sent: taskId={}, tripId={}, recipientType={}, recipientId={}, message={}",
                task.getId(),
                task.getTripId(),
                task.getRecipientType(),
                task.getRecipientId(),
                task.getMessage());
    }
}
