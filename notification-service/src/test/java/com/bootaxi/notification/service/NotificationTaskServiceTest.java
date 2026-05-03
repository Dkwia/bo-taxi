package com.bootaxi.notification.service;

import com.bootaxi.contracts.amqp.TripStatusChangedEvent;
import com.bootaxi.contracts.dto.NotificationCreateRequest;
import com.bootaxi.contracts.dto.NotificationResponse;
import com.bootaxi.contracts.enums.NotificationStatus;
import com.bootaxi.contracts.enums.RecipientType;
import com.bootaxi.contracts.enums.TripStatus;
import com.bootaxi.notification.domain.NotificationTask;
import com.bootaxi.notification.repository.NotificationTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTaskServiceTest {

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @InjectMocks
    private NotificationTaskService service;

    @Test
    void shouldCreatePendingNotificationTask() {
        when(notificationTaskRepository.save(any(NotificationTask.class))).thenAnswer(invocation -> {
            NotificationTask task = invocation.getArgument(0);
            task.setId(7L);
            return task;
        });

        NotificationResponse response = service.create(
                new NotificationCreateRequest(10L, RecipientType.PASSENGER, 100L, "Trip assigned"));

        assertEquals(7L, response.id());
        assertEquals(NotificationStatus.PENDING, response.status());
        assertEquals(0, response.attempts());
        assertEquals("Trip assigned", response.message());
    }

    @Test
    void shouldRejectBlankNotificationMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.create(new NotificationCreateRequest(10L, RecipientType.PASSENGER, 100L, " ")));

        assertEquals("Notification message is required", exception.getMessage());
    }

    @Test
    void shouldCreatePassengerAndDriverTasksFromTripEvent() {
        TripStatusChangedEvent event = new TripStatusChangedEvent(
                10L, 100L, 200L, TripStatus.DRIVER_ASSIGNED, new BigDecimal("12.50"), "Driver assigned", Instant.now());

        service.createFromTripEvent(event);

        ArgumentCaptor<List<NotificationTask>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationTaskRepository).saveAll(tasksCaptor.capture());
        List<NotificationTask> tasks = tasksCaptor.getValue();

        assertEquals(2, tasks.size());
        assertEquals(RecipientType.PASSENGER, tasks.get(0).getRecipientType());
        assertEquals(100L, tasks.get(0).getRecipientId());
        assertEquals(NotificationStatus.PENDING, tasks.get(0).getStatus());
        assertEquals(RecipientType.DRIVER, tasks.get(1).getRecipientType());
        assertEquals(200L, tasks.get(1).getRecipientId());
        assertEquals(0, tasks.get(1).getAttempts());
    }
}
