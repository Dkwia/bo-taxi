package com.bootaxi.notification.service;

import com.bootaxi.contracts.amqp.TripStatusChangedEvent;
import com.bootaxi.contracts.dto.NotificationCreateRequest;
import com.bootaxi.contracts.dto.NotificationResponse;
import com.bootaxi.contracts.enums.NotificationStatus;
import com.bootaxi.contracts.enums.RecipientType;
import com.bootaxi.notification.domain.NotificationTask;
import com.bootaxi.notification.repository.NotificationTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Transactional
    public NotificationResponse create(NotificationCreateRequest request) {
        NotificationTask task = new NotificationTask();
        task.setTripId(request.tripId());
        task.setRecipientType(request.recipientType());
        task.setRecipientId(request.recipientId());
        task.setMessage(requireText(request.message(), "Notification message is required"));
        task.setStatus(NotificationStatus.PENDING);
        task.setAttempts(0);
        return toResponse(notificationTaskRepository.save(task));
    }

    @Transactional
    public void createFromTripEvent(TripStatusChangedEvent event) {
        List<NotificationTask> tasks = new ArrayList<>();
        tasks.add(buildTask(event.tripId(), RecipientType.PASSENGER, event.passengerId(), event.message()));
        if (event.driverId() != null) {
            tasks.add(buildTask(event.tripId(), RecipientType.DRIVER, event.driverId(), event.message()));
        }
        notificationTaskRepository.saveAll(tasks);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByTripId(Long tripId) {
        return notificationTaskRepository.findByTripIdOrderByCreatedAtAsc(tripId).stream()
                .map(NotificationTaskService::toResponse)
                .toList();
    }

    private NotificationTask buildTask(Long tripId, RecipientType recipientType, Long recipientId, String message) {
        NotificationTask task = new NotificationTask();
        task.setTripId(tripId);
        task.setRecipientType(recipientType);
        task.setRecipientId(recipientId);
        task.setMessage(message);
        task.setStatus(NotificationStatus.PENDING);
        task.setAttempts(0);
        return task;
    }

    static NotificationResponse toResponse(NotificationTask task) {
        return new NotificationResponse(
                task.getId(),
                task.getTripId(),
                task.getRecipientType(),
                task.getRecipientId(),
                task.getMessage(),
                task.getStatus(),
                task.getAttempts(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
